package com.piggy.piggyfinance.factory;

import com.piggy.piggyfinance.enums.CategoryType;
import com.piggy.piggyfinance.model.Budget;
import com.piggy.piggyfinance.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class BudgetFixture {

    public static final UUID DEFAULT_ID = UUID.fromString("d1e2f3a4-b5c6-7890-abcd-ef1234567893");
    public static final CategoryType DEFAULT_CATEGORY = CategoryType.FOOD;
    public static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("500.00");
    public static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    public static final LocalDateTime DEFAULT_UPDATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);

    public static Budget create(User user) {
        return Budget.builder()
                .id(DEFAULT_ID)
                .user(user)
                .category(DEFAULT_CATEGORY)
                .amount(DEFAULT_AMOUNT)
                .createdAt(DEFAULT_CREATED_AT)
                .updatedAt(DEFAULT_UPDATED_AT)
                .build();
    }

    public static Budget.BudgetBuilder createBuilder(User user) {
        return create(user).toBuilder();
    }
}
