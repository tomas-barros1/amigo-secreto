package com.amigo.secreto.services;

import com.amigo.secreto.dtos.DrawResponseDTO;
import com.amigo.secreto.models.Draw;
import com.amigo.secreto.models.Group;
import com.amigo.secreto.models.User;
import com.amigo.secreto.repositories.DrawRepository;
import com.amigo.secreto.repositories.GroupRepository;
import com.amigo.secreto.services.exceptions.DrawAlreadyDoneException;
import com.amigo.secreto.services.exceptions.DrawPairNumberException;
import com.amigo.secreto.services.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DrawService {
    private final DrawRepository drawRepository;
    private final GroupRepository groupRepository;

    public DrawService(DrawRepository drawRepository, GroupRepository groupRepository) {
        this.drawRepository = drawRepository;
        this.groupRepository = groupRepository;
    }

    public Draw createDraw(UUID groupId) {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);

        if (optionalGroup.isEmpty()) {
            throw new ResourceNotFoundException("Grupo de id " + groupId + " não encontrado");
        }

        Group group = optionalGroup.get();

        if (group.isAlreadyDrawn()) {
            throw new DrawAlreadyDoneException("Sorteio já realizado para o grupo de id " + groupId);
        }

        List<User> participants = new ArrayList<>(group.getParticipants());

        if (participants.size() < 2) {
            throw new DrawPairNumberException("O grupo precisa ter pelo menos 2 participantes para realizar o sorteio.");
        }

        if (participants.size() % 2 != 0) {
            throw new DrawPairNumberException("Número de participantes precisa ser par");
        }

        Collections.shuffle(participants);

        Map<UUID, UUID> pairs = new HashMap<>();
        for (int i = 0; i < participants.size(); i++) {
            UUID giver = participants.get(i).getId();
            UUID receiver = participants.get((i + 1) % participants.size()).getId();
            pairs.put(giver, receiver);
        }

        Draw draw = new Draw();
        draw.setGroup(group);
        draw.setPairs(pairs);
        drawRepository.save(draw);

        group.setAlreadyDrawn(true);
        groupRepository.save(group);

        return draw;
    }

    public Optional<DrawResponseDTO> getDraw(UUID drawId) {
        Optional<Draw> drawOptional = drawRepository.findById(drawId);

        if (drawOptional.isEmpty()) {
            return Optional.empty();
        }

        Draw draw = drawOptional.get();
        Map<UUID, UUID> pairs = draw.getPairs();

        Map<String, String> pairsWithNames = pairs.entrySet().stream().collect(Collectors.toMap(
                entry -> getUserNameById(entry.getKey(), draw),
                entry -> getUserNameById(entry.getValue(), draw)
        ));

        DrawResponseDTO drawDTO = new DrawResponseDTO(draw.getId(), draw.getGroup(), pairsWithNames);
        return Optional.of(drawDTO);
    }

    private String getUserNameById(UUID userId, Draw draw) {
        return draw.getGroup().getParticipants().stream()
                .filter(user -> user.getId().equals(userId))
                .map(User::getName)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Usuário de id " + userId + "não encontrado"));
    }
}
