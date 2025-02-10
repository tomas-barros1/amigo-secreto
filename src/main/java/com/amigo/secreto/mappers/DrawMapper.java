package com.amigo.secreto.mappers;

import com.amigo.secreto.dtos.DrawResponseDTO;
import com.amigo.secreto.models.Draw;
import com.amigo.secreto.models.User;
import com.amigo.secreto.services.exceptions.ResourceNotFoundException;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DrawMapper {

    public static DrawResponseDTO toDrawResponseDTO(Draw draw) {
        Map<String, String> pairsWithNames = draw.getPairs().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> getUserNameById(entry.getKey(), draw),
                        entry -> getUserNameById(entry.getValue(), draw)
                ));

        return new DrawResponseDTO(draw.getId(), draw.getGroup(), pairsWithNames);
    }

    private static String getUserNameById(UUID userId, Draw draw) {
        return draw.getGroup().getParticipants().stream()
                .filter(user -> user.getId().equals(userId))
                .map(User::getName)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Usuário de id " + userId + " não encontrado"));
    }
    
}
