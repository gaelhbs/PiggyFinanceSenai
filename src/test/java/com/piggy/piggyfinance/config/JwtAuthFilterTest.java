package com.piggy.piggyfinance.config;

import com.piggy.piggyfinance.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

public class JwtAuthFilterTest {

    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        JwtService jwtService = mock(JwtService.class);
        jwtAuthFilter = new JwtAuthFilter(jwtService);
    }

    /**
     * Testa se ignora rotas públicas de autenticação.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldNotFilter_authRoute_returnsTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");

        boolean result = jwtAuthFilter.shouldNotFilter(request);

        assertThat(result).isTrue();
    }

    /**
     * Testa se aplica o filtro em rotas protegidas.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldNotFilter_protectedRoute_returnsFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/transactions");

        boolean result = jwtAuthFilter.shouldNotFilter(request);

        assertThat(result).isFalse();
    }

}
