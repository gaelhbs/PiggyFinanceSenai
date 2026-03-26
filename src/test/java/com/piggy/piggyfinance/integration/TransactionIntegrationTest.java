package com.piggy.piggyfinance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piggy.piggyfinance.enums.CategoryType;
import com.piggy.piggyfinance.enums.TransactionType;
import com.piggy.piggyfinance.model.requests.CreateTransactionRequest;
import com.piggy.piggyfinance.model.requests.LoginRequest;
import com.piggy.piggyfinance.model.requests.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TRANSACTIONS_URL = "/api/v1/transactions";
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("Gabriel", "gabriel@test.com", "senha123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest("gabriel@test.com", "senha123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        authToken = objectMapper.readTree(responseBody).get("token").asText();
    }

    /**
     * Testa se uma transação de receita é criada com sucesso via endpoint REST.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenCreateIncomeTransaction_givenValidRequest_shouldReturnCreated() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "Salário", new BigDecimal("5000.00"), TransactionType.INCOME, null);

        mockMvc.perform(post(TRANSACTIONS_URL + "/app")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("Salário"))
                .andExpect(jsonPath("$.amount").value(5000.00))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    /**
     * Testa se uma transação de despesa é criada com sucesso via endpoint REST.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenCreateExpenseTransaction_givenValidRequest_shouldReturnCreated() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "Almoço", new BigDecimal("35.50"), TransactionType.EXPENSE, CategoryType.FOOD);

        mockMvc.perform(post(TRANSACTIONS_URL + "/app")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("Almoço"))
                .andExpect(jsonPath("$.amount").value(35.50))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    /**
     * Testa se a criação de transação com dados inválidos retorna erro de validação.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenCreateTransaction_givenInvalidRequest_shouldReturnBadRequest() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "", null, null, null);

        mockMvc.perform(post(TRANSACTIONS_URL + "/app")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Testa se a listagem de transações retorna as transações do usuário autenticado.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenListTransactions_givenAuthenticatedUser_shouldReturnTransactions() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "Salário", new BigDecimal("5000.00"), TransactionType.INCOME, null);

        mockMvc.perform(post(TRANSACTIONS_URL + "/app")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(TRANSACTIONS_URL)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].description").value("Salário"));
    }

    /**
     * Testa se o resumo de transações retorna os valores corretos para o período informado.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenGetSummary_givenTransactionsInPeriod_shouldReturnCorrectSummary() throws Exception {
        CreateTransactionRequest income = new CreateTransactionRequest(
                "Salário", new BigDecimal("5000.00"), TransactionType.INCOME, null);
        CreateTransactionRequest expense = new CreateTransactionRequest(
                "Almoço", new BigDecimal("35.50"), TransactionType.EXPENSE, CategoryType.FOOD);

        mockMvc.perform(post(TRANSACTIONS_URL + "/app")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(income)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(TRANSACTIONS_URL + "/app")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isCreated());

        LocalDate today = LocalDate.now();
        mockMvc.perform(get(TRANSACTIONS_URL + "/summary")
                        .header("Authorization", "Bearer " + authToken)
                        .param("startDate", today.minusDays(1).toString())
                        .param("endDate", today.plusDays(1).toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.income").value(5000.00))
                .andExpect(jsonPath("$.expense").value(35.50))
                .andExpect(jsonPath("$.balance").value(4964.50));
    }

    /**
     * Testa se acessar transações sem token de autenticação retorna 401 ou 403.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenAccessTransactions_givenNoToken_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get(TRANSACTIONS_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
