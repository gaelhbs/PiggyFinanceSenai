package com.piggy.piggyfinance.controller;

import com.piggy.piggyfinance.model.Transaction;
import com.piggy.piggyfinance.model.requests.CreateTransactionRequest;
import com.piggy.piggyfinance.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid CreateTransactionRequest request, Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.status(201).body(transactionService.createTransaction(request, email));
    }

    @GetMapping
    public List<Transaction> list(Authentication authentication) {

        return transactionService.listTransactions(authentication.getName());
    }
}
