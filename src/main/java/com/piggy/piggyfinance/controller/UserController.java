package com.piggy.piggyfinance.controller;

import com.piggy.piggyfinance.model.responses.UserResponse;
import com.piggy.piggyfinance.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {

        UUID userId = (UUID) authentication.getPrincipal();

        return userService.getCurrentUser(userId);
    }
}
