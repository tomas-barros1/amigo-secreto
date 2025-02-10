package com.amigo.secreto.dtos;

import com.amigo.secreto.models.Group;
import java.util.Map;
import java.util.UUID;

public record DrawResponseDTO(
     UUID id,
     Group group,
     Map<String, String> pairs){
}
