package com.piggy.piggyfinance.model.dto;

import com.piggy.piggyfinance.enums.TransactionType;

import java.math.BigDecimal;

public record TransactionSummaryItem(
        TransactionType type,
        BigDecimal total
) {}
