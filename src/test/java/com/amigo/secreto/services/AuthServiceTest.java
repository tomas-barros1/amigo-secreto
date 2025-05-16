package com.amigo.secreto.services;

import com.amigo.secreto.dtos.LoginRequestDTO;
import com.amigo.secreto.dtos.LoginResponseDTO;
import com.amigo.secreto.dtos.RegisterRequestDTO;
import com.amigo.secreto.models.Role;
import com.amigo.secreto.models.User;
import com.amigo.secreto.repositories.RoleRepository;
import com.amigo.secreto.repositories.UserRepository;
import com.amigo.secreto.security.JwtService;
import com.amigo.secreto.services.exceptions.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private User adminUser;
    private Role userRole;
    private Role adminRole;
    private String testToken;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(new UUID(1L, 1L));
        userRole.setName("ROLE_USER");

        adminRole = new Role();
        adminRole.setId(new UUID(2L, 2L));
        adminRole.setName("ROLE_ADMIN");

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setWishItem("Test wish item");
        testUser.setRoles(Set.of(userRole));

        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername("adminuser");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("encodedPassword");
        adminUser.setRoles(Set.of(userRole, adminRole));

        testToken = "test-jwt-token";

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginResponse() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "password");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken("test@example.com")).thenReturn(testToken);

        // Act
        LoginResponseDTO response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getId().toString(), response.id());
        assertEquals(testUser.getEmail(), response.username()); // Mudado para email em vez de username
        assertEquals(testToken, response.token());
        assertTrue(response.roles().contains("ROLE_USER"));
        assertEquals(1, response.roles().size());

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password", "encodedPassword");
        verify(jwtService).generateToken("test@example.com");
    }

    @Test
    void login_WithInvalidEmail_ShouldThrowBadCredentialsException() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("nonexistent@example.com", "password");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.login(loginRequest));
        assertEquals("Usuário ou senha inválidos", exception.getMessage());

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowBadCredentialsException() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "wrongpassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.login(loginRequest));
        assertEquals("Usuário ou senha inválidos", exception.getMessage());

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("wrongpassword", "encodedPassword");
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Arrange
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "existinguser", "test@example.com", "password", "Existing user wish"
        );

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.register(registerRequest));
        assertEquals("Email já cadastrado", exception.getMessage());

        verify(userRepository).findByEmail("test@example.com");
        verify(roleRepository, never()).findByName(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void register_WithMissingUserRole_ShouldThrowException() {
        // Arrange
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "newuser", "new@example.com", "password", "New user wish"
        );

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> authService.register(registerRequest));
        assertEquals("Role 'ROLE_USER' não encontrada", exception.getMessage());

        verify(userRepository).findByEmail("new@example.com");
        verify(roleRepository).findByName("ROLE_USER");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void registerAdmin_AsNonAdmin_ShouldThrowAccessDeniedException() {
        // Arrange
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "newadmin", "newadmin@example.com", "password", null
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser); // Non-admin user

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> authService.registerAdmin(registerRequest));
        assertEquals("Apenas administradores podem criar novos administradores", exception.getMessage());

        verify(securityContext).getAuthentication();
        verify(authentication).getPrincipal();
        verify(userRepository, never()).findByEmail(anyString());
        verify(roleRepository, never()).findByName(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void registerAdmin_WithExistingEmail_ShouldThrowException() {
        // Arrange
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "existingadmin", "admin@example.com", "password", null
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.registerAdmin(registerRequest));
        assertEquals("Email já cadastrado", exception.getMessage());

        verify(securityContext).getAuthentication();
        verify(authentication).getPrincipal();
        verify(userRepository).findByEmail("admin@example.com");
        verify(roleRepository, never()).findByName(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void registerAdmin_WithMissingUserRole_ShouldThrowException() {
        // Arrange
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "newadmin", "newadmin@example.com", "password", null
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(userRepository.findByEmail("newadmin@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> authService.registerAdmin(registerRequest));
        assertEquals("Role 'ROLE_USER' não encontrada", exception.getMessage());

        verify(securityContext).getAuthentication();
        verify(authentication).getPrincipal();
        verify(userRepository).findByEmail("newadmin@example.com");
        verify(roleRepository).findByName("ROLE_USER");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void registerAdmin_WithMissingAdminRole_ShouldThrowException() {
        // Arrange
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "newadmin", "newadmin@example.com", "password", null
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(userRepository.findByEmail("newadmin@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> authService.registerAdmin(registerRequest));
        assertEquals("Role 'ROLE_ADMIN' não encontrada", exception.getMessage());

        verify(securityContext).getAuthentication();
        verify(authentication).getPrincipal();
        verify(userRepository).findByEmail("newadmin@example.com");
        verify(roleRepository).findByName("ROLE_USER");
        verify(roleRepository).findByName("ROLE_ADMIN");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(anyString());
    }
}