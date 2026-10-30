package org.fzl.transactionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients                                       ↑ valeur de secours après les deux-points
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TranslationServiceApplication.class, args);
    }

}
