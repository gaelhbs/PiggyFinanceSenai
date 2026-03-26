package com.piggy.piggyfinance.service.impl;

import com.piggy.piggyfinance.model.User;
import com.piggy.piggyfinance.model.requests.LoginRequest;
import com.piggy.piggyfinance.model.requests.RegisterRequest;
import com.piggy.piggyfinance.model.responses.LoginResponse;
import com.piggy.piggyfinance.model.responses.RegisterResponse;
import com.piggy.piggyfinance.repository.UserRepository;
import com.piggy.piggyfinance.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
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

    // Mocks das dependências — Mockito injeta tudo no @InjectMocks
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    // A classe que a gente realmente quer testar
    @InjectMocks
    private AuthServiceImpl authService;

    // Dados reutilizados nos testes
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User savedUser;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // Requests padrão pra register e login
        registerRequest = new RegisterRequest("John Doe", "john@email.com", "password123");
        loginRequest = new LoginRequest("john@email.com", "password123");

        // Usuário que o repositório "retorna" após salvar
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
        // Arrange - email livre, encoder e save mockados
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        RegisterResponse response = authService.register(registerRequest);

        // Assert - resposta populada e todos os colaboradores acionados
        assertThat(response).isNotNull();
        assertThat(response.user()).isEqualTo(savedUser.getId());
        assertThat(response.createdAt()).isEqualTo(savedUser.getCreatedAt());

        verify(userRepository).existsByEmail(registerRequest.email());
        verify(passwordEncoder).encode(registerRequest.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowRuntimeException_WhenEmailAlreadyExists() {
        // Arrange - email já cadastrado
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        // Act + Assert - lança exceção e não toca no save nem no encoder
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already exists");

        verify(userRepository).existsByEmail(registerRequest.email());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void register_ShouldEncodePassword_BeforeSaving() {
        // Arrange - garante que a senha nunca vai pro banco em texto puro
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        authService.register(registerRequest);

        // Assert - encoder foi chamado com a senha original antes do save
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("encodedPassword")
        ));
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        // Arrange - usuário existe e senha bate
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(loginRequest.password(), savedUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token-123");

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert - token gerado e todos os colaboradores chamados na ordem certa
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token-123");

        verify(userRepository).findByEmail(loginRequest.email());
        verify(passwordEncoder).matches(loginRequest.password(), savedUser.getPassword());
        verify(jwtService).generateToken(savedUser);
    }

    @Test
    void login_ShouldThrowRuntimeException_WhenEmailNotFound() {
        // Arrange - usuário não existe no banco
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        // Act + Assert - lança exceção genérica pra não vazar se o email existe ou não
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByEmail(loginRequest.email());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_ShouldThrowRuntimeException_WhenPasswordIsInvalid() {
        // Arrange - usuário existe mas senha errada
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(loginRequest.password(), savedUser.getPassword())).thenReturn(false);

        // Act + Assert - mesma mensagem do email não encontrado (segurança: não revela qual campo errou)
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByEmail(loginRequest.email());
        verify(passwordEncoder).matches(loginRequest.password(), savedUser.getPassword());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_ShouldNotGenerateToken_WhenAuthFails() {
        // Arrange - qualquer falha de autenticação (aqui por email inexistente)
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        // Act + Assert - jwtService nunca deve ser chamado se auth falhou
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class);

        verify(jwtService, never()).generateToken(any());
    }
}