package com.piggy.piggyfinance.factory;

import com.piggy.piggyfinance.enums.CategoryType;
import com.piggy.piggyfinance.model.requests.CreateBudgetRequest;

import java.math.BigDecimal;

public class CreateBudgetRequestFactory {

    public static final CategoryType DEFAULT_CATEGORY = CategoryType.FOOD;
    public static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("500.00");

    public static CreateBudgetRequest create() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CategoryType category = DEFAULT_CATEGORY;
        private BigDecimal amount = DEFAULT_AMOUNT;

        public Builder category(CategoryType category) {
            this.category = category;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public CreateBudgetRequest build() {
            return new CreateBudgetRequest(category, amount);
        }
    }
}
