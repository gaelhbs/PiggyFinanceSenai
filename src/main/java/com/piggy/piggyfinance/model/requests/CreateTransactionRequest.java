package com.piggy.piggyfinance.model.requests;

import com.piggy.piggyfinance.enums.CategoryType;
import com.piggy.piggyfinance.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateTransactionRequest(

        @NotBlank
        String description,

        @NotNull
        BigDecimal amount,

        @NotNull
        TransactionType type,

        CategoryType category
) {}
