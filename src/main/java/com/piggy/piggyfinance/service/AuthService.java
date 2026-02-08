package com.piggy.piggyfinance.service;

import com.piggy.piggyfinance.model.requests.LoginRequest;
import com.piggy.piggyfinance.model.requests.RegisterRequest;
import com.piggy.piggyfinance.model.responses.LoginResponse;
import com.piggy.piggyfinance.model.responses.RegisterResponse;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

     RegisterResponse register(RegisterRequest request);
     LoginResponse login(LoginRequest request);
}
