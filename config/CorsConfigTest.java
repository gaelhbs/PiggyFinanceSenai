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

    // Verifica se o Spring conseguiu criar e registrar o bean de CORS corretamente
    @Test
    void shouldCreateCorsConfigurationSourceBean() {
        assertNotNull(corsConfigurationSource);
    }

    // Verifica se existe exatamente uma origem permitida, evitando configurações acidentais
    @Test
    void shouldHaveExactlyOneAllowedOrigin() {
        assertEquals(1, config.getAllowedOrigins().size());
    }

    // Verifica se o domínio oficial da aplicação está autorizado a fazer requisições
    @Test
    void shouldAllowPiggyFinanceOrigin() {
        assertTrue(config.getAllowedOrigins().contains("https://piggyfinance.cloud"));
    }

    // Verifica se o localhost está bloqueado, impedindo acesso de ambientes locais não autorizados
    @Test
    void shouldNotAllowLocalhostOrigin() {
        assertFalse(config.getAllowedOrigins().contains("http://localhost:3000"));
    }

    // Verifica se domínios desconhecidos estão bloqueados, protegendo contra acessos externos maliciosos
    @Test
    void shouldNotAllowMaliciousOrigin() {
        assertFalse(config.getAllowedOrigins().contains("https://malicious.com"));
    }

    // Verifica se a versão HTTP (sem TLS) do domínio oficial também está bloqueada
    @Test
    void shouldNotAllowHttpVersionOfOfficialOrigin() {
        assertFalse(config.getAllowedOrigins().contains("http://piggyfinance.cloud"));
    }

    // Verifica se requisições GET estão liberadas para buscar dados da API
    @Test
    void shouldAllowGetMethod() {
        assertTrue(config.getAllowedMethods().contains("GET"));
    }

    // Verifica se requisições POST estão liberadas para criar novos recursos
    @Test
    void shouldAllowPostMethod() {
        assertTrue(config.getAllowedMethods().contains("POST"));
    }

    // Verifica se requisições PUT estão liberadas para atualizar recursos existentes
    @Test
    void shouldAllowPutMethod() {
        assertTrue(config.getAllowedMethods().contains("PUT"));
    }

    // Verifica se requisições DELETE estão liberadas para remover recursos
    @Test
    void shouldAllowDeleteMethod() {
        assertTrue(config.getAllowedMethods().contains("DELETE"));
    }

    // Verifica se OPTIONS está liberado, necessário para o preflight do navegador antes de requisições cross-origin
    @Test
    void shouldAllowOptionsMethod() {
        assertTrue(config.getAllowedMethods().contains("OPTIONS"));
    }

    // Verifica se há exatamente 5 métodos permitidos, evitando que métodos extras sejam adicionados acidentalmente
    @Test
    void shouldHaveExactlyFiveAllowedMethods() {
        assertEquals(5, config.getAllowedMethods().size());
    }

    // Verifica se PATCH está bloqueado pois não foi incluído nas configurações da API
    @Test
    void shouldNotAllowPatchMethod() {
        assertFalse(config.getAllowedMethods().contains("PATCH"));
    }

    // Verifica se TRACE está bloqueado pois pode expor informações sensíveis da requisição
    @Test
    void shouldNotAllowTraceMethod() {
        assertFalse(config.getAllowedMethods().contains("TRACE"));
    }

    // Verifica se todos os headers são permitidos, necessário para enviar Authorization, Content-Type, etc.
    @Test
    void shouldAllowAllHeaders() {
        assertTrue(config.getAllowedHeaders().contains("*"));
    }

    // Verifica se existe exatamente uma regra de header, confirmando que só o wildcard foi configurado
    @Test
    void shouldHaveExactlyOneHeaderRule() {
        assertEquals(1, config.getAllowedHeaders().size());
    }

    // Verifica se credentials está habilitado, permitindo o envio de cookies e tokens de autenticação
    @Test
    void shouldAllowCredentials() {
        assertNotNull(config.getAllowCredentials());
        assertTrue(config.getAllowCredentials());
    }
}
