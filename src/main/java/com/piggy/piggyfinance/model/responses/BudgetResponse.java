package com.piggy.piggyfinance.model.responses;

import com.piggy.piggyfinance.enums.CategoryType;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetResponse(UUID id, CategoryType category, BigDecimal amount) {}