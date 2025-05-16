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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        User user = this.userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new BadCredentialsException("Usuário ou senha inválidos"));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new BadCredentialsException("Usuário ou senha inválidos");
        }

        String token = this.jwtService.generateToken(user.getUsername());
        return new LoginResponseDTO(user.getId().toString(), user.getUsername(), token, user.getRoles().stream().map(Role::getName).toList());
    }

    public LoginResponseDTO register(RegisterRequestDTO registerRequest) {
        Optional<User> existingUser = this.userRepository.findByEmail(registerRequest.email());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Role 'ROLE_USER' não encontrada"));

        User newUser = new User();
        newUser.setUsername(registerRequest.username());
        newUser.setEmail(registerRequest.email());
        newUser.setWishItem(registerRequest.wishItem());
        newUser.setPassword(passwordEncoder.encode(registerRequest.password()));
        newUser.setRoles(Set.of(userRole));

        this.userRepository.save(newUser);

        String token = this.jwtService.generateToken(newUser.getEmail());
        return new LoginResponseDTO(newUser.getId().toString() ,newUser.getUsername(), token, newUser.getRoles().stream().map(Role::getName).toList());
    }

    public LoginResponseDTO registerAdmin(RegisterRequestDTO registerRequest) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AccessDeniedException("Apenas administradores podem criar novos administradores");
        }

        Optional<User> existingUser = this.userRepository.findByEmail(registerRequest.email());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Role 'ROLE_USER' não encontrada"));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("Role 'ROLE_ADMIN' não encontrada"));

        User newAdmin = new User();
        newAdmin.setUsername(registerRequest.username());
        newAdmin.setEmail(registerRequest.email());
        newAdmin.setPassword(passwordEncoder.encode(registerRequest.password()));
        newAdmin.setRoles(Set.of(userRole, adminRole));

        this.userRepository.save(newAdmin);

        String token = this.jwtService.generateToken(newAdmin.getEmail());
        return new LoginResponseDTO(newAdmin.getId().toString(), newAdmin.getUsername(), token, newAdmin.getRoles().stream().map(Role::getName).toList());
    }
}
