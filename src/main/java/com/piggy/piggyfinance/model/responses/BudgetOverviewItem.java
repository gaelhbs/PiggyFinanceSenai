package com.piggy.piggyfinance.model.responses;

import com.piggy.piggyfinance.enums.CategoryType;

import java.math.BigDecimal;

public record BudgetOverviewItem(
        CategoryType category,
        BigDecimal budgeted,
        BigDecimal spent,
        BigDecimal remaining,
        boolean exceeded
) {}