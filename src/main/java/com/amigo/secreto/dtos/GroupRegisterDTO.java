package com.amigo.secreto.dtos;

import java.util.UUID;

public record GroupRegisterDTO(String name, UUID ownerId) {
}
