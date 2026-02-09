package com.piggy.piggyfinance.service;

import com.piggy.piggyfinance.model.User;

import java.security.Key;
import java.util.UUID;

public interface JwtService {
    String generateToken(User user);
    boolean isTokenValid(String token);
    UUID extractUserId(String token);
    Key getSigningKey();
}

