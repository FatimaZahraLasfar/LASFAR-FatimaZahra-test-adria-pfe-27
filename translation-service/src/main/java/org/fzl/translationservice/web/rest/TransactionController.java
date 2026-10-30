package org.fzl.translationservice.web.rest;

import java.util.List;
import java.util.UUID;


import org.fzl.translationservice.service.TransactionService;
import org.fzl.translationservice.service.dto.CreateTransactionRequest;
import org.fzl.translationservice.service.dto.TransactionResponse;
import org.fzl.translationservice.service.dto.TransferRequest;
import org.fzl.translationservice.service.dto.TransferResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@RequestBody CreateTransactionRequest request) {
        return service.create(request);
    }

    @GetMapping("/transactions")
    public List<TransactionResponse> list(@RequestParam(required = false) UUID accountId) {
        return service.findAll(accountId);
    }

    @GetMapping("/transactions/{id}")
    public TransactionResponse get(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(@RequestBody TransferRequest request) {
        return service.transfer(request);
    }
}
