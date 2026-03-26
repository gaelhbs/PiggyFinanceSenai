package com.piggy.piggyfinance.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CorsConfig.class)
class CorsConfigTest {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    private CorsConfiguration config;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        config = corsConfigurationSource.getCorsConfiguration(request);
        assertNotNull(config, "CorsConfiguration não deveria ser nula");
    }

    /**
     * Testa se o Spring conseguiu criar e registrar o bean de CORS corretamente.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldCreateCorsConfigurationSourceBean() {
        assertNotNull(corsConfigurationSource);
    }

    /**
     * Testa se existe exatamente uma origem permitida, evitando configurações acidentais.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldHaveExactlyOneAllowedOrigin() {
        assertEquals(1, config.getAllowedOrigins().size());
    }

    /**
     * Testa se o domínio oficial da aplicação está autorizado a fazer requisições.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldAllowPiggyFinanceOrigin() {
        assertTrue(config.getAllowedOrigins().contains("https://piggyfinance.cloud"));
    }

    /**
     * Testa se o localhost está bloqueado, impedindo acesso de ambientes locais não autorizados.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldNotAllowLocalhostOrigin() {
        assertFalse(config.getAllowedOrigins().contains("http://localhost:3000"));
    }

    /**
     * Testa se domínios desconhecidos estão bloqueados, protegendo contra acessos externos maliciosos.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldNotAllowMaliciousOrigin() {
        assertFalse(config.getAllowedOrigins().contains("https://malicious.com"));
    }

    /**
     * Testa se a versão HTTP (sem TLS) do domínio oficial também está bloqueada.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldNotAllowHttpVersionOfOfficialOrigin() {
        assertFalse(config.getAllowedOrigins().contains("http://piggyfinance.cloud"));
    }

    /**
     * Testa se requisições GET estão liberadas para buscar dados da API.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldAllowGetMethod() {
        assertTrue(config.getAllowedMethods().contains("GET"));
    }

    /**
     * Testa se requisições POST estão liberadas para criar novos recursos.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldAllowPostMethod() {
        assertTrue(config.getAllowedMethods().contains("POST"));
    }

    /**
     * Testa se requisições PUT estão liberadas para atualizar recursos existentes.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldAllowPutMethod() {
        assertTrue(config.getAllowedMethods().contains("PUT"));
    }

    /**
     * Testa se requisições DELETE estão liberadas para remover recursos.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldAllowDeleteMethod() {
        assertTrue(config.getAllowedMethods().contains("DELETE"));
    }

    /**
     * Testa se OPTIONS está liberado, necessário para o preflight do navegador antes de requisições cross-origin.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldAllowOptionsMethod() {
        assertTrue(config.getAllowedMethods().contains("OPTIONS"));
    }

    /**
     * Testa se há exatamente 5 métodos permitidos, evitando que métodos extras sejam adicionados acidentalmente.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldHaveExactlyFiveAllowedMethods() {
        assertEquals(5, config.getAllowedMethods().size());
    }

    /**
     * Testa se PATCH está bloqueado pois não foi incluído nas configurações da API.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldNotAllowPatchMethod() {
        assertFalse(config.getAllowedMethods().contains("PATCH"));
    }

    /**
     * Testa se TRACE está bloqueado pois pode expor informações sensíveis da requisição.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldNotAllowTraceMethod() {
        assertFalse(config.getAllowedMethods().contains("TRACE"));
    }

    /**
     * Testa se todos os headers são permitidos, necessário para enviar Authorization, Content-Type, etc.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldAllowAllHeaders() {
        assertTrue(config.getAllowedHeaders().contains("*"));
    }

    /**
     * Testa se existe exatamente uma regra de header, confirmando que só o wildcard foi configurado.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldHaveExactlyOneHeaderRule() {
        assertEquals(1, config.getAllowedHeaders().size());
    }

    /**
     * Testa se credentials está habilitado, permitindo o envio de cookies e tokens de autenticação.
     * Criado por: thauanlima1 em 24/03/2026
     */
    @Test
    void shouldAllowCredentials() {
        assertNotNull(config.getAllowCredentials());
        assertTrue(config.getAllowCredentials());
    }
}
