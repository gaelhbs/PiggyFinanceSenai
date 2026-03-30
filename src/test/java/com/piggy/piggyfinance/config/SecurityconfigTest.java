package com.piggy.piggyfinance.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    // Só tá aqui pra o @InjectMocks não reclamar
    @Mock
    private JwtAuthFilter jwtAuthFilter;

    // A classe que a gente quer testar, injetando o mock acima
    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    @DisplayName("passwordEncoder: deve retornar uma instância de BCryptPasswordEncoder")
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        // Arrange - não tem nada a preparar, a config já foi injetada

        // Act - chama o método que cria o bean
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // Assert - garante que voltou um BCrypt e não null
        assertThat(encoder).isNotNull();
        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    @DisplayName("passwordEncoder: deve encodar e validar senha corretamente")
    void passwordEncoder_ShouldEncodeAndMatchPassword() {
        // Arrange - pega o encoder e define a senha de teste
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String rawPassword = "mySecret123";

        // Act - encodar a senha (BCrypt gera hash diferente a cada vez)
        String encoded = encoder.encode(rawPassword);

        // Assert - hash é diferente do original, matches funciona certo
        assertThat(encoded).isNotEqualTo(rawPassword);
        assertThat(encoder.matches(rawPassword, encoded)).isTrue();
        assertThat(encoder.matches("wrongPassword", encoded)).isFalse();
    }

    @Test
    @DisplayName("passwordEncoder: dois hashes da mesma senha devem ser diferentes (salt aleatório)")
    void passwordEncoder_ShouldGenerateDifferentHashesForSamePassword() {
        // Arrange - mesmo encoder, mesma senha
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String rawPassword = "samePassword";

        // Act - encodar duas vezes
        String encoded1 = encoder.encode(rawPassword);
        String encoded2 = encoder.encode(rawPassword);

        // Assert - BCrypt usa salt aleatório, então os hashes nunca são iguais
        assertThat(encoded1).isNotEqualTo(encoded2);
    }

    @Test
    @DisplayName("passwordEncoder: hash gerado deve começar com prefixo BCrypt '$2a$'")
    void passwordEncoder_ShouldProduceBCryptFormattedHash() {
        // Arrange
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // Act
        String encoded = encoder.encode("qualquerCoisa");

        // Assert - confirma que o formato é realmente BCrypt
        assertThat(encoded).startsWith("$2a$");
    }

    @Test
    @DisplayName("passwordEncoder: cada chamada ao bean deve retornar um encoder funcional independente")
    void passwordEncoder_MultipleCallsShouldReturnFunctionalEncoders() {
        // Arrange - cria dois encoders separados (simula múltiplos beans no contexto)
        PasswordEncoder encoder1 = securityConfig.passwordEncoder();
        PasswordEncoder encoder2 = securityConfig.passwordEncoder();
        String password = "testMulti";

        // Act
        String hash = encoder1.encode(password);

        // Assert - encoder2 consegue validar hash feito pelo encoder1
        assertThat(encoder2.matches(password, hash)).isTrue();
    }

    @Test
    @DisplayName("passwordEncoder: senha com caracteres especiais deve ser encodada e validada corretamente")
    void passwordEncoder_ShouldHandleSpecialCharactersPassword() {
        // Arrange
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String specialPassword = "P@$$w0rd!#%&*()";

        // Act
        String encoded = encoder.encode(specialPassword);

        // Assert
        assertThat(encoded).isNotNull();
        assertThat(encoded).startsWith("$2a$");
        assertThat(encoder.matches(specialPassword, encoded)).isTrue();
        assertThat(encoder.matches("outraCoisa", encoded)).isFalse();
    }

    @Test
    @DisplayName("passwordEncoder: senha acima de 72 bytes deve lançar IllegalArgumentException")
    void passwordEncoder_ShouldRejectPasswordLongerThan72Bytes() {
        // Arrange - Spring Boot 4 passou a rejeitar senhas acima do limite do BCrypt
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String tooLongPassword = "a".repeat(73);

        // Act + Assert - estoura o limite, joga exceção
        assertThatThrownBy(() -> encoder.encode(tooLongPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("password cannot be more than 72 bytes");
    }
}