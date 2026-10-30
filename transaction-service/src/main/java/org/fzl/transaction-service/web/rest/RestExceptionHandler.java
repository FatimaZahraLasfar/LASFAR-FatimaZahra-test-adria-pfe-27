package org.fzl.translationservice.web.rest;

import org.fzl.translationservice.exception.BankException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(BankException.class)
    public ProblemDetail handle(BankException ex) {
        return ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
    }
}
