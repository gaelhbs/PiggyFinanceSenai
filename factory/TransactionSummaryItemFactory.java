package com.piggy.piggyfinance.factory;

import com.piggy.piggyfinance.enums.TransactionType;
import com.piggy.piggyfinance.model.dto.TransactionSummaryItem;

import java.math.BigDecimal;

public class TransactionSummaryItemFactory {

    public static final BigDecimal DEFAULT_INCOME = new BigDecimal("1000.00");
    public static final BigDecimal DEFAULT_EXPENSE = new BigDecimal("400.00");

    public static TransactionSummaryItem createIncome() {
        return builder().build();
    }

    public static TransactionSummaryItem createExpense() {
        return builder()
                .type(TransactionType.EXPENSE)
                .total(DEFAULT_EXPENSE)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TransactionType type = TransactionType.INCOME;
        private BigDecimal total = DEFAULT_INCOME;

        public Builder type(TransactionType type) {
            this.type = type;
            return this;
        }

        public Builder total(BigDecimal total) {
            this.total = total;
            return this;
        }

        public TransactionSummaryItem build() {
            return new TransactionSummaryItem(type, total);
        }
    }
}