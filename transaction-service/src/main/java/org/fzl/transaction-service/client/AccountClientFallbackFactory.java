package org.fzl.transactionservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.fzl.translationservice.exception.BankException;
import org.slf4j.Logger;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountClientFallbackFactory implements FallbackFactory<AccountClient> {

    private static final Logger log = LoggerFactory.getLogger(AccountClientFallbackFactory.class);

    private final ObjectMapper mapper;

    public AccountClientFallbackFactory(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AccountClient create(Throwable cause) {
        return new AccountClient() {
            @Override
            public AccountDto getAccount(UUID id) {
                throw translate("getAccount", cause);
            }

            @Override
            public AccountDto debit(UUID id, AmountRequest request) {
                throw translate("debit", cause);
            }

            @Override
            public AccountDto credit(UUID id, AmountRequest request) {
                throw translate("credit", cause);
            }
        };
    }

    private BankException translate(String operation, Throwable cause) {
        if (cause instanceof FeignException fe && fe.status() >= 400 && fe.status() < 500) {
            return new BankException(HttpStatus.valueOf(fe.status()), extractDetail(fe));
        }
        if (cause instanceof CallNotPermittedException) {
            log.warn("Circuit breaker OUVERT pour account-service ({})", operation);
            return new BankException(HttpStatus.SERVICE_UNAVAILABLE,
                    "account-service est temporairement indisponible (circuit ouvert), réessayez plus tard");
        }
        log.warn("Échec de l'appel account-service ({}) : {}", operation, cause.toString());
        return new BankException(HttpStatus.SERVICE_UNAVAILABLE,
                "account-service est indisponible, la transaction n'a pas été enregistrée");
    }

    private String extractDetail(FeignException fe) {
        try {
            return mapper.readTree(fe.contentUTF8()).path("detail").asText(fe.getMessage());
        } catch (Exception e) {
            return fe.getMessage();
        }
    }
}