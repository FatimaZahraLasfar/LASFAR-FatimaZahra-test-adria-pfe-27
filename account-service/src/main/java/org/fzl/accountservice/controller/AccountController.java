package org.fzl.accountservice.controller;

import org.fzl.accountservice.service.AccountService;
import org.fzl.accountservice.service.dto.AccountResponse;
import org.fzl.accountservice.service.dto.AmountRequest;
import org.fzl.accountservice.service.dto.CreateAccountRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping
    public List<AccountResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@RequestBody CreateAccountRequest request) {
        return service.create(request);
    }

    @PostMapping("/{id}/block")
    public AccountResponse block(@PathVariable UUID id) {
        return service.block(id);
    }

    @PostMapping("/{id}/activate")
    public AccountResponse activate(@PathVariable UUID id) {
        return service.activate(id);
    }

    /** Utilisé par transaction-service (OpenFeign). */
    @PostMapping("/{id}/debit")
    public AccountResponse debit(@PathVariable UUID id, @RequestBody AmountRequest request) {
        return service.debit(id, request.amount());
    }

    /** Utilisé par transaction-service (OpenFeign). */
    @PostMapping("/{id}/credit")
    public AccountResponse credit(@PathVariable UUID id, @RequestBody AmountRequest request) {
        return service.credit(id, request.amount());
    }
}
