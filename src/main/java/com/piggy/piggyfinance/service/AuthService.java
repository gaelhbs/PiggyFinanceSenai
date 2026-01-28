package com.piggy.piggyfinance.service;

import com.piggy.piggyfinance.model.requests.LoginRequest;
import com.piggy.piggyfinance.model.requests.RegisterRequest;
import com.piggy.piggyfinance.model.responses.AuthResponse;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    public AuthResponse register(RegisterRequest request);
    public AuthResponse login(LoginRequest request);
}
