package org.fzl.accountservice.service.dto;

public record CreateAccountRequest(String customerReference, String currency, double initialBalance) {
}
