package com.amigo.secreto.controllers;

import com.amigo.secreto.dtos.LoginRequestDTO;
import com.amigo.secreto.dtos.LoginResponseDTO;
import com.amigo.secreto.dtos.RegisterRequestDTO;
import com.amigo.secreto.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação e registro de usuários")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login de usuário", description = "Realiza o login de um usuário com base nas credenciais fornecidas.")
    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar usuário", description = "Registra um novo usuário no sistema.")
    @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso")
    public LoginResponseDTO register(@RequestBody RegisterRequestDTO registerRequest) {
        return authService.register(registerRequest);
    }

    @PostMapping("/register/admin")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar usuário admin", description = "Registra um novo usuário com o papel de administrador.")
    @ApiResponse(responseCode = "201", description = "Usuário admin registrado com sucesso")
    public LoginResponseDTO registerAdmin(@RequestBody RegisterRequestDTO registerRequest) {
        return authService.registerAdmin(registerRequest);
    }
}