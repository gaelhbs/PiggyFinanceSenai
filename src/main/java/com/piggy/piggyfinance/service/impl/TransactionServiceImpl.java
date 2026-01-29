package com.piggy.piggyfinance.service.impl;

import com.piggy.piggyfinance.exceptions.BusinessException;
import com.piggy.piggyfinance.exceptions.UserNotFoundException;
import com.piggy.piggyfinance.model.Transaction;
import com.piggy.piggyfinance.model.User;
import com.piggy.piggyfinance.model.filters.TransactionFilter;
import com.piggy.piggyfinance.model.requests.CreateTransactionRequest;
import com.piggy.piggyfinance.repository.TransactionRepository;
import com.piggy.piggyfinance.repository.UserRepository;
import com.piggy.piggyfinance.repository.specifications.TransactionSpecification;
import com.piggy.piggyfinance.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    public Transaction createTransaction(CreateTransactionRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));

        validate(request);

        Transaction transaction = Transaction.builder()
                .description(request.description())
                .amount(request.amount())
                .type(request.type())
                .timestamp(LocalDateTime.now())
                .user(user)
                .build();

        return transactionRepository.save(transaction);
    }

    private void validate(CreateTransactionRequest request){

        if(request.amount().compareTo(BigDecimal.ZERO) <= 0){
            throw new BusinessException("Transaction amount must be greater than zero");
        }
    }

    @Override
    public Page<Transaction> listTransactions(String email, TransactionFilter filter, Pageable pageable) {

         return transactionRepository.findAll(TransactionSpecification.byFilter(filter,email),pageable);
    }
}
