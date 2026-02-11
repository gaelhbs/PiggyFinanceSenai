package com.piggy.piggyfinance.model.responses;

import java.math.BigDecimal;

public record TransactionSummaryResponse(
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance
) {}
