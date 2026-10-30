package org.fzl.translationservice.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Format minimal de l'événement publié dans bank.transactions.created. */
public record TransactionCreatedEvent(UUID eventId, String eventType, UUID transactionId, UUID accountId,
                                      BigDecimal amount, String currency, Instant occurredAt) {

    public static TransactionCreatedEvent of(UUID transactionId, UUID accountId, BigDecimal amount,
                                             String currency, Instant occurredAt) {
        return new TransactionCreatedEvent(UUID.randomUUID(), "TransactionCreated", transactionId,
                accountId, amount, currency, occurredAt);
    }
}
