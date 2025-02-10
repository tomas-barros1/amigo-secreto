package com.amigo.secreto.services;

import com.amigo.secreto.models.User;
import com.amigo.secreto.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("Should create a user successfully")
    void userCreteTest() {
        User user = new User(UUID.randomUUID(), "Joao", "joao13@gmail.com", "senha123", "coxinhas", null);

        when(userRepository.save(user)).thenReturn(user);

        userService.create(user);

        assertNotNull(user);
        assertEquals("Joao", user.getName());
        assertEquals("joao13@gmail.com", user.getEmail());
    }

    @Test
    @DisplayName("Should delete a user successfully")
    void userDeleteTest() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Joao", "joao13@gmail.com", "senha123", "coxinha", null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.delete(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("Update user")


}