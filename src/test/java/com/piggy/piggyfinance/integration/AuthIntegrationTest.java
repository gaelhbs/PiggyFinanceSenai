package com.piggy.piggyfinance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piggy.piggyfinance.model.requests.LoginRequest;
import com.piggy.piggyfinance.model.requests.RegisterRequest;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String REGISTER_URL = "/api/auth/register";
    private static final String LOGIN_URL = "/api/auth/login";

    /**
     * Testa se o registro de um novo usuário retorna sucesso com os dados corretos.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenRegister_givenValidRequest_shouldReturnSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest("Gabriel", "gabriel@email.com", "senha123");

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    /**
     * Testa se o registro com email duplicado lança exceção.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenRegister_givenDuplicateEmail_shouldThrowException() throws Exception {
        RegisterRequest request = new RegisterRequest("Gabriel", "duplicado@email.com", "senha123");

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertThatThrownBy(() -> mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .isInstanceOf(ServletException.class)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");
    }

    /**
     * Testa se o login com credenciais válidas retorna um token JWT.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenLogin_givenValidCredentials_shouldReturnToken() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("Gabriel", "login@email.com", "senha123");
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest("login@email.com", "senha123");
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    /**
     * Testa se o login com senha incorreta lança exceção.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenLogin_givenWrongPassword_shouldThrowException() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("Gabriel", "wrong@email.com", "senha123");
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest("wrong@email.com", "senhaErrada");
        assertThatThrownBy(() -> mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))))
                .isInstanceOf(ServletException.class)
                .hasCauseInstanceOf(RuntimeException.class);
    }

    /**
     * Testa se acessar uma rota protegida sem token retorna 401 ou 403.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenAccessProtectedRoute_givenNoToken_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}