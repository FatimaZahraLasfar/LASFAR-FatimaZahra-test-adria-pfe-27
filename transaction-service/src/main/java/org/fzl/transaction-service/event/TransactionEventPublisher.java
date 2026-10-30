package org.fzl.transactionservice.event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TransactionEventPublisher {

    public static final String TOPIC = "bank.transactions.created";

    private static final Logger log = LoggerFactory.getLogger(TransactionEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper;

    public TransactionEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper mapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.mapper = mapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionCreated(TransactionCreatedEvent event) {
        try {
            String payload = mapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, event.accountId().toString(), payload)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error("Échec de publication de l'événement {} : {}", event.eventId(), error.toString());
                        } else {
                            log.info("Événement {} publié dans {} (partition {}, offset {})", event.eventId(), TOPIC,
                                    result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Sérialisation impossible de l'événement {}", event.eventId(), e);
        }
    }
}
