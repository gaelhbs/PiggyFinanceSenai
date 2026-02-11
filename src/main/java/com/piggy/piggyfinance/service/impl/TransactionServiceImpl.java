package com.piggy.piggyfinance.service.impl;

import com.piggy.piggyfinance.enums.TransactionSourceEnum;
import com.piggy.piggyfinance.enums.TransactionType;
import com.piggy.piggyfinance.exceptions.BusinessException;
import com.piggy.piggyfinance.factory.TransactionFactory;
import com.piggy.piggyfinance.model.Transaction;
import com.piggy.piggyfinance.model.User;
import com.piggy.piggyfinance.model.filters.TransactionFilter;
import com.piggy.piggyfinance.model.requests.CreateTransactionRequest;
import com.piggy.piggyfinance.model.responses.TransactionSummaryResponse;
import com.piggy.piggyfinance.repository.TransactionRepository;
import com.piggy.piggyfinance.repository.UserRepository;
import com.piggy.piggyfinance.repository.specifications.TransactionSpecification;
import com.piggy.piggyfinance.service.TransactionService;
import com.piggy.piggyfinance.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    public Transaction createTransaction(CreateTransactionRequest request, TransactionSourceEnum source) {
        validate(request);

        UUID userId = SecurityUtils.getAuthenticatedUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction transaction =
                TransactionFactory.create(request, source, user);

        return transactionRepository.save(transaction);
    }

    @Override
    public Page<Transaction> listTransactions(TransactionFilter filter, Pageable pageable) {
        UUID userId = SecurityUtils.getAuthenticatedUserId();

        return transactionRepository.findAll(
                TransactionSpecification.byFilter(filter, userId),
                pageable
        );
    }

    @Override
    public TransactionSummaryResponse getSummary(UUID userId, LocalDate startDate, LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        var result = transactionRepository.getSummary(userId, start, end);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (var item : result) {
            if (item.type() == TransactionType.INCOME) {
                totalIncome = item.total();
            } else if (item.type() == TransactionType.EXPENSE) {
                totalExpense = item.total();
            }
        }

        BigDecimal balance = totalIncome.subtract(totalExpense);

        return new TransactionSummaryResponse(
                totalIncome,
                totalExpense,
                balance
        );
    }

    private void validate(CreateTransactionRequest request) {

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Transaction amount must be greater than zero");
        }
    }
}
