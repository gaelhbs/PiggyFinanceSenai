package com.piggy.piggyfinance.model.responses;

import java.math.BigDecimal;
import java.util.List;

public record BudgetOverviewResponse(
        List<BudgetOverviewItem> items,
        BigDecimal totalBudgeted,
        BigDecimal totalSpent
) {}