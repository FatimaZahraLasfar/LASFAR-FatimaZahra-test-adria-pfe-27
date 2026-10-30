package org.fzl.accountservice.service.dto;

import org.fzl.accountservice.entities.Account;
import org.fzl.accountservice.entities.AccountStatus;

import java.util.UUID;

public record AccountResponse(UUID id, String customerReference, AccountStatus status,
                              String currency, double balance) {

    public static AccountResponse from(Account a) {
        return new AccountResponse(a.getId(), a.getCustomerReference(), a.getStatus(),
                a.getCurrency(), a.getBalance());
    }
}
