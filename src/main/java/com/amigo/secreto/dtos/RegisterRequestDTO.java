package com.amigo.secreto.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record RegisterRequestDTO(
        String username,
        @Email(message = "Email inválido")
        @Pattern(regexp = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", message = "Formato de e-mail inválido")
        String email,
        String password,
        String wishItem
) {
}
