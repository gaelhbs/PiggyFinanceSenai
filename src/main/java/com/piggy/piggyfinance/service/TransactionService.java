package com.piggy.piggyfinance.service;

import com.piggy.piggyfinance.model.Transaction;
import com.piggy.piggyfinance.model.requests.CreateTransactionRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TransactionService {

    public Transaction createTransaction(CreateTransactionRequest request, String email);

    public List<Transaction> listTransactions(String email);
}
