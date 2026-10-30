package org.fzl.transactionservice.config;

import org.fzl.transactionservice.event.TransactionEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic transactionsCreatedTopic() {
        return TopicBuilder.name(TransactionEventPublisher.TOPIC).partitions(3).replicas(1).build();
    }
}
