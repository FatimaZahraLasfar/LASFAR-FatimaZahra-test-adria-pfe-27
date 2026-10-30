package org.fzl.accountservice.service;

import org.fzl.accountservice.entities.Account;
import org.fzl.accountservice.entities.AccountStatus;
import org.fzl.accountservice.exception.BankException;
import org.fzl.accountservice.repository.AccountRepository;
import org.fzl.accountservice.service.dto.AccountResponse;
import org.fzl.accountservice.service.dto.CreateAccountRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findAll() {
        return repository.findAll().stream().map(AccountResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse findById(UUID id) {
        return AccountResponse.from(load(id));
    }

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        if (request == null || request.customerReference() == null || request.customerReference().isBlank()) {
            throw new BankException(HttpStatus.BAD_REQUEST, "customerReference est obligatoire");
        }
        if (request.currency() == null || request.currency().length() != 3) {
            throw new BankException(HttpStatus.BAD_REQUEST, "currency doit être un code ISO sur 3 lettres (ex : EUR)");
        }
        if (request.initialBalance() < 0) {
            throw new BankException(HttpStatus.BAD_REQUEST, "Le solde initial ne peut pas être négatif");
        }
        Account account = new Account(UUID.randomUUID(), request.customerReference().trim(),
                AccountStatus.ACTIVE, request.currency().toUpperCase(), request.initialBalance());
        return AccountResponse.from(repository.save(account));
    }

    @Transactional
    public AccountResponse block(UUID id) {
        Account account = load(id);
        account.setStatus(AccountStatus.BLOCKED);
        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse activate(UUID id) {
        Account account = load(id);
        account.setStatus(AccountStatus.ACTIVE);
        return AccountResponse.from(account);
    }

    /** Débite le compte (le solde doit être suffisant et le compte actif). */
    @Transactional
    public AccountResponse debit(UUID id, BigDecimal amount) {
        requirePositive(amount);
        Account account = loadForUpdate(id);
        requireActive(account);
        BigDecimal newBalance = BigDecimal.valueOf(account.getBalance()).subtract(amount);
        if (newBalance.signum() < 0) {
            throw new BankException(HttpStatus.UNPROCESSABLE_ENTITY, "Solde insuffisant");
        }
        account.setBalance(round(newBalance));
        return AccountResponse.from(account);
    }

    /** Crédite le compte (le compte doit être actif). */
    @Transactional
    public AccountResponse credit(UUID id, BigDecimal amount) {
        requirePositive(amount);
        Account account = loadForUpdate(id);
        requireActive(account);
        account.setBalance(round(BigDecimal.valueOf(account.getBalance()).add(amount)));
        return AccountResponse.from(account);
    }

    private Account load(UUID id) {
        return repository.findById(id).orElseThrow(notFound(id));
    }

    private Account loadForUpdate(UUID id) {
        return repository.findByIdForUpdate(id).orElseThrow(notFound(id));
    }

    private static java.util.function.Supplier<BankException> notFound(UUID id) {
        return () -> new BankException(HttpStatus.NOT_FOUND, "Compte introuvable : " + id);
    }

    private static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BankException(HttpStatus.BAD_REQUEST, "Le montant doit être strictement positif");
        }
    }

    private static void requireActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BankException(HttpStatus.CONFLICT, "Le compte " + account.getId() + " est bloqué");
        }
    }

    private static double round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
