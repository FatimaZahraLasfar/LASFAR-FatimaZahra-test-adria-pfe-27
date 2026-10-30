package org.fzl.accountservice.service.dto;

import java.util.Objects;

public final class CreateAccountRequest {

    private final String customerReference;
    private final String currency;
    private final double initialBalance;

    public CreateAccountRequest(String customerReference, String currency, double initialBalance) {
        this.customerReference = customerReference;
        this.currency = currency;
        this.initialBalance = initialBalance;
    }

    public String customerReference() { return customerReference; }
    public String currency()          { return currency; }
    public double initialBalance()    { return initialBalance; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CreateAccountRequest that)) return false;
        return Double.compare(that.initialBalance, initialBalance) == 0
                && Objects.equals(customerReference, that.customerReference)
                && Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerReference, currency, initialBalance);
    }

    @Override
    public String toString() {
        return "CreateAccountRequest[customerReference=" + customerReference +
                ", currency=" + currency + ", initialBalance=" + initialBalance + "]";
    }
}