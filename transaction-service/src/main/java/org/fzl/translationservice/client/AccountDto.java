package org.fzl.translationservice.client;

import java.util.UUID;

/** Vue d'un compte telle que renvoyée par account-service. */
public record AccountDto(UUID id, String customerReference, String status, String currency, double balance) {

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
