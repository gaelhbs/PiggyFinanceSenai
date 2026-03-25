package com.piggy.piggyfinance.model.requests;

import java.math.BigDecimal;

public record UpdateBudgetRequest(BigDecimal amount) {}