package com.piggy.piggyfinance.service;

import com.piggy.piggyfinance.enums.TransactionSourceEnum;
import com.piggy.piggyfinance.model.Transaction;
import com.piggy.piggyfinance.model.filters.TransactionFilter;
import com.piggy.piggyfinance.model.requests.CreateTransactionRequest;
import com.piggy.piggyfinance.model.responses.TransactionSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public interface TransactionService {

    Transaction createTransaction(CreateTransactionRequest request, TransactionSourceEnum source);

    Page<Transaction> listTransactions(TransactionFilter filter, Pageable pageable);

    TransactionSummaryResponse getSummary(UUID userId, LocalDate startDate, LocalDate endDate);
}
