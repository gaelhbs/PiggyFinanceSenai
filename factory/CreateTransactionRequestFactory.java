package com.piggy.piggyfinance.factory;

import com.piggy.piggyfinance.enums.CategoryType;
import com.piggy.piggyfinance.enums.TransactionType;
import com.piggy.piggyfinance.model.requests.CreateTransactionRequest;

import java.math.BigDecimal;

public class CreateTransactionRequestFactory {

    public static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("100.00");
    public static final String DEFAULT_DESCRIPTION = "Test transaction";

    public static CreateTransactionRequest createIncome() {
        return builder().build();
    }

    public static CreateTransactionRequest createExpense() {
        return builder()
                .type(TransactionType.EXPENSE)
                .category(CategoryType.FOOD)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String description = DEFAULT_DESCRIPTION;
        private BigDecimal amount = DEFAULT_AMOUNT;
        private TransactionType type = TransactionType.INCOME;
        private CategoryType category = null;

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder type(TransactionType type) {
            this.type = type;
            return this;
        }

        public Builder category(CategoryType category) {
            this.category = category;
            return this;
        }

        public CreateTransactionRequest build() {
            return new CreateTransactionRequest(description, amount, type, category);
        }
    }
}