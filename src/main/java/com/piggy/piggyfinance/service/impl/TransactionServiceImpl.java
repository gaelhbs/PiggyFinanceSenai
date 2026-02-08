package com.piggy.piggyfinance.service.impl;

import com.piggy.piggyfinance.enums.TransactionSourceEnum;
import com.piggy.piggyfinance.exceptions.BusinessException;
import com.piggy.piggyfinance.factory.TransactionFactory;
import com.piggy.piggyfinance.model.Transaction;
import com.piggy.piggyfinance.model.User;
import com.piggy.piggyfinance.model.filters.TransactionFilter;
import com.piggy.piggyfinance.model.requests.CreateTransactionRequest;
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
        return transactionRepository.findAll(
                TransactionSpecification.byFilter(filter),
                pageable
        );
    }

    private void validate(CreateTransactionRequest request){

        if(request.amount().compareTo(BigDecimal.ZERO) <= 0){
            throw new BusinessException("Transaction amount must be greater than zero");
        }
    }
}
