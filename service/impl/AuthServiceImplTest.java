package com.piggy.piggyfinance.service.impl;

import com.piggy.piggyfinance.model.User;
import com.piggy.piggyfinance.model.requests.LoginRequest;
import com.piggy.piggyfinance.model.requests.RegisterRequest;
import com.piggy.piggyfinance.model.responses.LoginResponse;
import com.piggy.piggyfinance.model.responses.RegisterResponse;
import com.piggy.piggyfinance.repository.UserRepository;
import com.piggy.piggyfinance.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User savedUser;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("John Doe", "john@email.com", "password123");
        loginRequest = new LoginRequest("john@email.com", "password123");

        savedUser = User.builder()
                .id(userId)
                .name("John Doe")
                .email("john@email.com")
                .password("encodedPassword")
                .createdAt(LocalDateTime.now())
                .build();
    }


    @Test
    void register_ShouldRegisterUser_WhenEmailNotExists() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.user()).isEqualTo(savedUser.getId());
        assertThat(response.createdAt()).isEqualTo(savedUser.getCreatedAt());

        verify(userRepository).existsByEmail(registerRequest.email());
        verify(passwordEncoder).encode(registerRequest.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowRuntimeException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already exists");

        verify(userRepository).existsByEmail(registerRequest.email());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(loginRequest.password(), savedUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token-123");

        LoginResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token-123");

        verify(userRepository).findByEmail(loginRequest.email());
        verify(passwordEncoder).matches(loginRequest.password(), savedUser.getPassword());
        verify(jwtService).generateToken(savedUser);
    }

    @Test
    void login_ShouldThrowRuntimeException_WhenEmailNotFound() {
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByEmail(loginRequest.email());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_ShouldThrowRuntimeException_WhenPasswordIsInvalid() {
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(loginRequest.password(), savedUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByEmail(loginRequest.email());
        verify(passwordEncoder).matches(loginRequest.password(), savedUser.getPassword());
        verify(jwtService, never()).generateToken(any());
    }
}
