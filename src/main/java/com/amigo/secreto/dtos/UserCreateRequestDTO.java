package com.amigo.secreto.dtos;

import jakarta.validation.constraints.Email;

public record UserCreateRequestDTO(String name, @Email String email, String password) {
}
