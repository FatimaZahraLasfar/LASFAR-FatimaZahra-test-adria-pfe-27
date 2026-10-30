package org.fzl.translationservice.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


import org.fzl.translationservice.client.AccountClient;
import org.fzl.translationservice.client.AccountDto;
import org.fzl.translationservice.client.AmountRequest;
import org.fzl.translationservice.entities.Transaction;
import org.fzl.translationservice.event.TransactionCreatedEvent;
import org.fzl.translationservice.exception.BankException;
import org.fzl.translationservice.repository.TransactionRepository;
import org.fzl.translationservice.service.dto.CreateTransactionRequest;
import org.fzl.translationservice.service.dto.TransactionResponse;
import org.fzl.translationservice.service.dto.TransferRequest;
import org.fzl.translationservice.service.dto.TransferResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Couche métier unique, appelée par les contrôleurs REST, GraphQL et par les outils MCP.
 *
 * Règles : montant > 0 et <= bank.transaction.max-amount (Config Server), compte existant et actif,
 * devise identique à celle du compte, consultation du solde avant chaque transaction, mise à jour du solde
 * après l'enregistrement, événement Kafka publié après le commit.
 */
@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository repository;
    private final AccountClient accountClient;
    private final ApplicationEventPublisher events;
    private final BigDecimal maxAmount;

    public TransactionService(TransactionRepository repository,
                              AccountClient accountClient,
                              ApplicationEventPublisher events,
                              @Value("${bank.transaction.max-amount}") BigDecimal maxAmount) {
        this.repository = repository;
        this.accountClient = accountClient;
        this.events = events;
        this.maxAmount = maxAmount;
    }

    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {
        if (request == null || request.accountId() == null) {
            throw new BankException(HttpStatus.BAD_REQUEST, "accountId est obligatoire");
        }
        validateAmount(request.amount());
        validateCurrencyPresent(request.currency());

        // 1. Consultation du compte et de son solde (OpenFeign + Resilience4J)
        AccountDto account = accountClient.getAccount(request.accountId());
        requireActive(account);
        requireSameCurrency(account, request.currency());
        requireSufficientBalance(account, request.amount());

        // 2. Enregistrement de la transaction
        Transaction transaction = save(request.accountId(), request.amount(), request.currency(),
                request.merchantCategory(), request.country());

        // 3. Mise à jour du solde ; une erreur ici annule l'enregistrement (rollback)
        accountClient.debit(request.accountId(), new AmountRequest(request.amount()));

        // 4. Événement Kafka, envoyé seulement après le commit
        publish(transaction);
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        if (request == null || request.fromAccountId() == null || request.toAccountId() == null) {
            throw new BankException(HttpStatus.BAD_REQUEST, "fromAccountId et toAccountId sont obligatoires");
        }
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new BankException(HttpStatus.BAD_REQUEST, "Le compte source et le compte destination doivent différer");
        }
        validateAmount(request.amount());
        validateCurrencyPresent(request.currency());

        AccountDto from = accountClient.getAccount(request.fromAccountId());
        AccountDto to = accountClient.getAccount(request.toAccountId());
        requireActive(from);
        requireActive(to);
        requireSameCurrency(from, request.currency());
        requireSameCurrency(to, request.currency());
        requireSufficientBalance(from, request.amount());

        Transaction out = save(request.fromAccountId(), request.amount(), request.currency(), "TRANSFER_OUT", null);
        Transaction in = save(request.toAccountId(), request.amount(), request.currency(), "TRANSFER_IN", null);

        AmountRequest amount = new AmountRequest(request.amount());
        accountClient.debit(request.fromAccountId(), amount);
        try {
            accountClient.credit(request.toAccountId(), amount);
        } catch (RuntimeException e) {
            // Compensation : on rend l'argent au compte source, puis on annule l'enregistrement
            try {
                accountClient.credit(request.fromAccountId(), amount);
            } catch (RuntimeException compensationError) {
                log.error("COMPENSATION ÉCHOUÉE pour le virement {} -> {} ({}) : vérification manuelle requise",
                        request.fromAccountId(), request.toAccountId(), request.amount(), compensationError);
            }
            throw e;
        }

        publish(out);
        publish(in);
        return new TransferResponse(TransactionResponse.from(out), TransactionResponse.from(in));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll(UUID accountId) {
        List<Transaction> list = accountId == null
                ? repository.findAllByOrderByCreatedAtDesc()
                : repository.findByAccountIdOrderByCreatedAtDesc(accountId);
        return list.stream().map(TransactionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse findById(UUID id) {
        return repository.findById(id)
                .map(TransactionResponse::from)
                .orElseThrow(() -> new BankException(HttpStatus.NOT_FOUND, "Transaction introuvable : " + id));
    }

    // ---------------------------------------------------------------- règles métier

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BankException(HttpStatus.BAD_REQUEST, "Le montant doit être strictement positif");
        }
        if (amount.compareTo(maxAmount) > 0) {
            throw new BankException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Le montant dépasse le plafond autorisé (" + maxAmount.toPlainString() + ")");
        }
    }

    private static void validateCurrencyPresent(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new BankException(HttpStatus.BAD_REQUEST, "currency est obligatoire");
        }
    }

    private static void requireActive(AccountDto account) {
        if (!account.isActive()) {
            throw new BankException(HttpStatus.CONFLICT, "Le compte " + account.id() + " n'est pas actif");
        }
    }

    private static void requireSameCurrency(AccountDto account, String currency) {
        if (!account.currency().equalsIgnoreCase(currency)) {
            throw new BankException(HttpStatus.UNPROCESSABLE_ENTITY, "La devise " + currency
                    + " ne correspond pas à celle du compte " + account.id() + " (" + account.currency() + ")");
        }
    }

    private static void requireSufficientBalance(AccountDto account, BigDecimal amount) {
        if (BigDecimal.valueOf(account.balance()).compareTo(amount) < 0) {
            throw new BankException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Solde insuffisant (solde : " + account.balance() + ", montant : " + amount.toPlainString() + ")");
        }
    }

    private Transaction save(UUID accountId, BigDecimal amount, String currency, String merchantCategory, String country) {
        Transaction transaction = new Transaction(UUID.randomUUID(), accountId, amount, currency.toUpperCase(),
                merchantCategory, country, Instant.now());
        return repository.save(transaction);
    }

    private void publish(Transaction t) {
        events.publishEvent(TransactionCreatedEvent.of(t.getId(), t.getAccountId(), t.getAmount(),
                t.getCurrency(), t.getCreatedAt()));
    }
}
