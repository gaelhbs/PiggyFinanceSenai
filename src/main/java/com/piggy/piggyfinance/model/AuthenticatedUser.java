package com.piggy.piggyfinance.model;

import java.util.UUID;

public record AuthenticatedUser(UUID userId,
                                String email) {
}
