package com.piggy.piggyfinance.model.filters;

import com.piggy.piggyfinance.enums.TransactionType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionFilter {

    private TransactionType type;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}
