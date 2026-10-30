package org.fzl.eventconsumerservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventListener.class);
    private final ObjectMapper mapper;

    public TransactionEventListener(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @KafkaListener(topics = "bank.transactions.created", groupId = "event-consumer")
    public void onMessage(@Payload String payload,
                          @Header(KafkaHeaders.RECEIVED_KEY) String key,
                          @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                          @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            JsonNode event = mapper.readTree(payload);
            log.info("[EVENT RECU] type={} eventId={} transactionId={} accountId={} amount={} {} occurredAt={} (clé={}, partition={}, offset={})",
                    event.path("eventType").asText(),
                    event.path("eventId").asText(),
                    event.path("transactionId").asText(),
                    event.path("accountId").asText(),
                    event.path("amount").asText(),
                    event.path("currency").asText(),
                    event.path("occurredAt").asText(),
                    key, partition, offset);
        } catch (Exception e) {
            log.error("Événement illisible (clé={}, offset={}) : {}", key, offset, payload, e);
        }
    }
}
