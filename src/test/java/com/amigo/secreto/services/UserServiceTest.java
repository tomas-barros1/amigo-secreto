package com.amigo.secreto.services;

import com.amigo.secreto.models.User;
import com.amigo.secreto.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("Update user successfully")
    void userUpdateTest() {
        UUID userId = UUID.randomUUID();
        User updatedUser = new User(userId, "João Silva", "joao13@gmail.com", "novaSenha", "pastel", null, null);

        when(userRepository.save(updatedUser)).thenReturn(updatedUser);

        User result = userService.update(updatedUser);

        assertNotNull(result);
        assertEquals("novaSenha", result.getPassword());
        assertEquals("joao13@gmail.com", result.getEmail());
        assertEquals("pastel", result.getWishItem());

        verify(userRepository, times(1)).save(updatedUser);
    }

    @Test
    @DisplayName("Should delete a user successfully")
    void userDeleteTest() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Joao", "joao13@gmail.com", "senha123", "coxinha", null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.delete(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }
}
