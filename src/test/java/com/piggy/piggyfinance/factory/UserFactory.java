package com.piggy.piggyfinance.factory;

import com.piggy.piggyfinance.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserFactory {

    public static final UUID DEFAULT_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    public static final String DEFAULT_NAME = "John Doe";
    public static final String DEFAULT_EMAIL = "john.doe@example.com";
    public static final String DEFAULT_PASSWORD = "encoded_password";
    public static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);

    public static User createUser() {
        return User.builder()
                .id(DEFAULT_ID)
                .name(DEFAULT_NAME)
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    public static User.UserBuilder createUserBuilder() {
        return createUser().toBuilder();
    }
}