package com.piggy.piggyfinance.model.responses;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegisterResponse(
        UUID user,
        String message,
        LocalDateTime createdAt) {
}
