package com.piggy.piggyfinance.service.impl;

import com.piggy.piggyfinance.model.User;
import com.piggy.piggyfinance.model.requests.LoginRequest;
import com.piggy.piggyfinance.model.requests.RegisterRequest;
import com.piggy.piggyfinance.model.responses.LoginResponse;
import com.piggy.piggyfinance.model.responses.RegisterResponse;
import com.piggy.piggyfinance.repository.UserRepository;
import com.piggy.piggyfinance.service.AuthService;
import com.piggy.piggyfinance.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .createdAt(LocalDateTime.now())
                .build();


        User savedUser = userRepository.save(user);

        return new RegisterResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getCreatedAt());
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(token);
    }
}
