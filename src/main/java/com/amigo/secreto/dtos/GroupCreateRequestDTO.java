package com.amigo.secreto.dtos;

import java.util.UUID;

public record GroupCreateRequestDTO(String name, UUID ownerId) {
}
