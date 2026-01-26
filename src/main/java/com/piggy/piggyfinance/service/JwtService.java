package com.piggy.piggyfinance.service;

import com.piggy.piggyfinance.model.User;
import org.springframework.stereotype.Service;

@Service
public interface JwtService {

    public String generateToken(User user);
    public String extractUserId(String token);
}
