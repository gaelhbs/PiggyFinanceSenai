package com.piggy.piggyfinance.factory;

import com.piggy.piggyfinance.enums.TransactionSourceEnum;
import com.piggy.piggyfinance.enums.TransactionType;
import com.piggy.piggyfinance.model.Transaction;
import com.piggy.piggyfinance.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionFixture {

    public static final UUID DEFAULT_ID = UUID.fromString("c1d2e3f4-a5b6-7890-abcd-ef1234567892");
    public static final String DEFAULT_DESCRIPTION = "Test transaction";
    public static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("100.00");
    public static final LocalDateTime DEFAULT_TIMESTAMP = LocalDateTime.of(2024, 1, 15, 10, 0);

    public static Transaction create(User user) {
        return Transaction.builder()
                .id(DEFAULT_ID)
                .description(DEFAULT_DESCRIPTION)
                .amount(DEFAULT_AMOUNT)
                .type(TransactionType.INCOME)
                .source(TransactionSourceEnum.APP)
                .category(null)
                .timestamp(DEFAULT_TIMESTAMP)
                .user(user)
                .build();
    }

    public static Transaction.TransactionBuilder createBuilder(User user) {
        return create(user).toBuilder();
    }
}