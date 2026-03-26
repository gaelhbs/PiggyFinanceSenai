package com.piggy.piggyfinance.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class JwtUtilsTest {

    private static final String SECRET = "bXlTdXBlclNlY3JldEtleUZvclBpZ2d5RmluYW5jZUFwcDIwMjQ=";
    private static final UUID EXPECTED_USER_ID = UUID.randomUUID();

    private JwtUtils jwtUtils;
    private String VALID_TOKEN;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(SECRET);


        byte[] keyBytes = Base64.getDecoder().decode(SECRET);

        VALID_TOKEN = Jwts.builder()
                .claim("userId", EXPECTED_USER_ID.toString())
                .signWith(SignatureAlgorithm.HS256, keyBytes)
                .compact();
    }

    /**
     * Testa se um token válido retorna o userId correto.
     * Criado por: ThauanLima1 em 24/03/2026
     */
    @Test
    void success_validToken_returnsCorrectUserId() {
        UUID extractedUserId = jwtUtils.getUserId(VALID_TOKEN);

        assertThat(extractedUserId).isEqualTo(EXPECTED_USER_ID);
    }

    /**
     * Testa se consegue acusar um token adulterado.
     * Criado por: ThauanLima1 em 24/03/2026
     */
    @Test
    void failure_tamperedToken_throwsException() {
        String tamperedToken = VALID_TOKEN + "adulterado";

        assertThatThrownBy(() -> jwtUtils.getUserId(tamperedToken))
                .isInstanceOf(Exception.class);
    }

    /**
     * Testa se rejeita uma string que não é um token JWT.
     * Criado por: ThauanLima1 em 24/03/2026
     */
    @Test
    void failure_invalidToken_throwsException() {
        assertThatThrownBy(() -> jwtUtils.getUserId("this.is.not.a.jwt"))
                .isInstanceOf(Exception.class);
    }

    /**
     * Testa se lança exceção ao tentar ler um userId que não existe no token.
     * Criado por: ThauanLima1 em 24/03/2026
     */
    @Test
    void failure_tokenWithoutUserId_throwsException() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET);
        String tokenWithoutUserId = Jwts.builder()
                .claim("outrocampo", "valor")
                .signWith(SignatureAlgorithm.HS256, keyBytes)
                .compact();
        assertThatThrownBy(() -> jwtUtils.getUserId(tokenWithoutUserId))
                .isInstanceOf(Exception.class);
    }

}