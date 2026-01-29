package com.piggy.piggyfinance.service;

import com.piggy.piggyfinance.model.Transaction;
import com.piggy.piggyfinance.model.filters.TransactionFilter;
import com.piggy.piggyfinance.model.requests.CreateTransactionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface TransactionService {

    Transaction createTransaction(CreateTransactionRequest request, String email);

    Page<Transaction> listTransactions(String email, TransactionFilter filter , Pageable pageable);
}
