package org.fzl.transactionservice.mcp;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fzl.transactionservice.service.TransactionService;
import org.fzl.transactionservice.service.dto.CreateTransactionRequest;
import org.fzl.transactionservice.service.dto.TransferRequest;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Outils MCP exposés par transaction-service. Ils appellent la même couche service que REST et GraphQL.
 * Chaque outil renvoie du JSON, ou un message "ERREUR : ..." que le LLM peut expliquer à l'utilisateur.
 */
@Component
public class TransactionMcpTools {

    private final TransactionService service;
    private final ObjectMapper mapper;

    public TransactionMcpTools(TransactionService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Tool(description = "Enregistre une transaction (paiement) qui débite un compte. "
            + "Le montant doit être > 0 et respecter le plafond, le compte actif, la devise identique à celle du compte.")
    public String createTransaction(
            @ToolParam(description = "UUID du compte à débiter") String accountId,
            @ToolParam(description = "Montant strictement positif") double amount,
            @ToolParam(description = "Devise ISO, ex : EUR") String currency,
            @ToolParam(description = "Catégorie du commerçant, ex : GROCERY, TRAVEL, ELECTRONICS", required = false) String merchantCategory,
            @ToolParam(description = "Code pays ISO du commerçant, ex : FR, MA", required = false) String country) {
        return run(() -> service.create(new CreateTransactionRequest(UUID.fromString(accountId),
                BigDecimal.valueOf(amount), currency, merchantCategory, country)));
    }

    @Tool(description = "Effectue un virement d'un compte vers un autre compte (même devise).")
    public String transfer(
            @ToolParam(description = "UUID du compte source") String fromAccountId,
            @ToolParam(description = "UUID du compte destination") String toAccountId,
            @ToolParam(description = "Montant strictement positif") double amount,
            @ToolParam(description = "Devise ISO, ex : EUR") String currency) {
        return run(() -> service.transfer(new TransferRequest(UUID.fromString(fromAccountId),
                UUID.fromString(toAccountId), BigDecimal.valueOf(amount), currency)));
    }

    @Tool(description = "Liste les transactions d'un compte, de la plus récente à la plus ancienne.")
    public String listTransactionsByAccount(@ToolParam(description = "UUID du compte") String accountId) {
        return run(() -> service.findAll(UUID.fromString(accountId)));
    }

    @Tool(description = "Liste toutes les transactions, de la plus récente à la plus ancienne.")
    public String listAllTransactions() {
        return run(() -> service.findAll(null));
    }

    @Tool(description = "Récupère une transaction à partir de son identifiant UUID.")
    public String getTransaction(@ToolParam(description = "UUID de la transaction") String transactionId) {
        return run(() -> service.findById(UUID.fromString(transactionId)));
    }

    private String run(java.util.function.Supplier<Object> action) {
        try {
            return mapper.writeValueAsString(action.get());
        } catch (Exception e) {
            return "ERREUR : " + e.getMessage();
        }
    }
}
