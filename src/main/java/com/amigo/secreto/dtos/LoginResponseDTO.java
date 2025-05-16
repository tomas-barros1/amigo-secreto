package com.amigo.secreto.dtos;

import java.util.List;

public record LoginResponseDTO(
        String id,
        String username,
        String token,
        List<String> roles
) {}