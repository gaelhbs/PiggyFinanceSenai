package com.piggy.piggyfinance.model.requests;

import com.piggy.piggyfinance.enums.CategoryType;

import java.math.BigDecimal;

public record CreateBudgetRequest(CategoryType category, BigDecimal amount) {}
