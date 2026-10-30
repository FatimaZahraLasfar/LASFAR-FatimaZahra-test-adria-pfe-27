package org.fzl.accountservice;

import org.fzl.accountservice.entities.Account;
import org.fzl.accountservice.entities.AccountStatus;
import org.fzl.accountservice.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

@SpringBootApplication
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }

    /** Données de démonstration avec des UUID fixes pour faciliter les tests. */
    @Bean
    CommandLineRunner seed(AccountRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            repository.save(new Account(UUID.fromString("11111111-1111-1111-1111-111111111111"),
                    "CUST-001", AccountStatus.ACTIVE, "EUR", 5000.0));
            repository.save(new Account(UUID.fromString("22222222-2222-2222-2222-222222222222"),
                    "CUST-002", AccountStatus.ACTIVE, "EUR", 1000.0));
            repository.save(new Account(UUID.fromString("33333333-3333-3333-3333-333333333333"),
                    "CUST-003", AccountStatus.BLOCKED, "EUR", 300.0));
            repository.save(new Account(UUID.fromString("44444444-4444-4444-4444-444444444444"),
                    "CUST-004", AccountStatus.ACTIVE, "USD", 2000.0));
        };
    }
}
