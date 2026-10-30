package org.fzl.transactionservice.exception;


import org.springframework.http.HttpStatus;

/** Erreur métier portant le statut HTTP à renvoyer. */
public class BankException extends RuntimeException {

    private final HttpStatus status;

    public BankException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
