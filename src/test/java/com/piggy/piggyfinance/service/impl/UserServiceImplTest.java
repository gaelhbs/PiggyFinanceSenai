package com.piggy.piggyfinance.service.impl;

import com.piggy.piggyfinance.factory.UserFactory;
import com.piggy.piggyfinance.model.User;
import com.piggy.piggyfinance.model.responses.UserResponse;
import com.piggy.piggyfinance.repository.UserRepository;
import com.piggy.piggyfinance.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void whenGetCurrentUser_givenValidUserId_shouldReturnUserResponseWithCorrectData() {
        // Arrange
        User user = UserFactory.createUser();
        when(userRepository.findById(UserFactory.DEFAULT_ID)).thenReturn(Optional.of(user));

        // Act
        UserResponse response = userService.getCurrentUser(UserFactory.DEFAULT_ID);

        // Assert
        assertThat(response.id()).isEqualTo(UserFactory.DEFAULT_ID);
        assertThat(response.name()).isEqualTo(UserFactory.DEFAULT_NAME);
        assertThat(response.email()).isEqualTo(UserFactory.DEFAULT_EMAIL);
        verify(userRepository).findById(UserFactory.DEFAULT_ID);
    }

    @Test
    void whenGetCurrentUser_givenNonExistentUserId_shouldThrowRuntimeExceptionWithMessage() {
        // Arrange
        UUID nonExistentId = UserFactory.createUserBuilder()
                .id(UUID.randomUUID())
                .build()
                .getId();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getCurrentUser(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
        verify(userRepository).findById(nonExistentId);
    }
}