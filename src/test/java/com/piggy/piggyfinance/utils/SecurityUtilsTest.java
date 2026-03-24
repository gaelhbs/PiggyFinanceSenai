package com.piggy.piggyfinance.utils;

import com.piggy.piggyfinance.exceptions.UserNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    //Verifica se retorna o userId correto quando o usuário está autenticado

    @Test
    void success_authenticatedUser_returnsCorrectUserId() {
        UUID expectedUserId = UUID.randomUUID();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                expectedUserId,
                null,
                List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UUID result = SecurityUtils.getAuthenticatedUserId();

        assertThat(result).isEqualTo(expectedUserId);
    }

    //Se o usuario nao estiver logado, lance uma exceção

    @Test
    void failure_noAuthentication_throwsException() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> SecurityUtils.getAuthenticatedUserId())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not authenticated");
    }

    //Verifica se lança exceção quando o principal não é um UUID

    @Test
    void failure_invalidPrincipal_throwsException() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "not a uuid",
                null,
                List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThatThrownBy(() -> SecurityUtils.getAuthenticatedUserId())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid authentication principal");
    }


}