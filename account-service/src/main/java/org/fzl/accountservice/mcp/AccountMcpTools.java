package org.fzl.accountservice.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fzl.accountservice.service.AccountService;
import org.fzl.accountservice.service.dto.CreateAccountRequest;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.ai.tool.annotation.Tool;

import java.util.UUID;

@Component
public class AccountMcpTools {

    private final AccountService service;
    private final ObjectMapper mapper;

    public AccountMcpTools(AccountService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Tool(description = "Liste tous les comptes bancaires (id, référence client, statut, devise, solde).")
    public String listAccounts() {
        return run(service::findAll);
    }

    @Tool(description = "Récupère un compte bancaire (statut, devise, solde) à partir de son identifiant UUID.")
    public String getAccount(@ToolParam(description = "Identifiant UUID du compte") String accountId) {
        return run(() -> service.findById(UUID.fromString(accountId)));
    }

    @Tool(description = "Crée un nouveau compte bancaire actif.")
    public String createAccount(
            @ToolParam(description = "Référence du client, ex : CUST-010") String customerReference,
            @ToolParam(description = "Devise ISO sur 3 lettres, ex : EUR") String currency,
            @ToolParam(description = "Solde initial (>= 0)") double initialBalance) {
        return run(() -> service.create(new CreateAccountRequest(customerReference, currency, initialBalance)));
    }

    @Tool(description = "Bloque un compte bancaire : plus aucune transaction n'est alors possible.")
    public String blockAccount(@ToolParam(description = "Identifiant UUID du compte") String accountId) {
        return run(() -> service.block(UUID.fromString(accountId)));
    }

    @Tool(description = "Réactive un compte bancaire bloqué.")
    public String activateAccount(@ToolParam(description = "Identifiant UUID du compte") String accountId) {
        return run(() -> service.activate(UUID.fromString(accountId)));
    }

    private String run(java.util.function.Supplier<Object> action) {
        try {
            return mapper.writeValueAsString(action.get());
        } catch (Exception e) {
            return "ERREUR : " + e.getMessage();
        }
    }
}