package com.amigo.secreto.services;

import com.amigo.secreto.dtos.DrawResponseDTO;
import com.amigo.secreto.mappers.DrawMapper;
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

@Service
public class DrawService {
    private final DrawRepository drawRepository;
    private final GroupRepository groupRepository;

    public DrawService(DrawRepository drawRepository, GroupRepository groupRepository) {
        this.drawRepository = drawRepository;
        this.groupRepository = groupRepository;
    }

    public DrawResponseDTO createDraw(UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo de id " + groupId + " não encontrado"));

        validateDrawConditions(group);

        Map<UUID, UUID> pairs = generatePairs(group.getParticipants());

        Draw draw = new Draw();
        draw.setGroup(group);
        draw.setPairs(pairs);
        drawRepository.save(draw);

        group.setAlreadyDrawn(true);
        groupRepository.save(group);

        return DrawMapper.toDrawResponseDTO(draw);
    }

    public DrawResponseDTO getDraw(UUID drawId) {
        Draw draw = drawRepository.findById(drawId)
                .orElseThrow(() -> new ResourceNotFoundException("Sorteio de id " + drawId + " não encontrado"));
        return DrawMapper.toDrawResponseDTO(draw);
    }

    private void validateDrawConditions(Group group) {
        if (group.isAlreadyDrawn()) {
            throw new DrawAlreadyDoneException("Sorteio já realizado para o grupo de id " + group.getId());
        }

        List<User> participants = group.getParticipants();
        if (participants.size() < 2) {
            throw new DrawPairNumberException("O grupo precisa ter pelo menos 2 participantes para realizar o sorteio.");
        }

        if (participants.size() % 2 != 0) {
            throw new DrawPairNumberException("Número de participantes precisa ser par");
        }
    }

    private Map<UUID, UUID> generatePairs(List<User> participants) {
        Collections.shuffle(participants);

        Map<UUID, UUID> pairs = new HashMap<>();
        for (int i = 0; i < participants.size(); i++) {
            UUID giver = participants.get(i).getId();
            UUID receiver = participants.get((i + 1) % participants.size()).getId();
            pairs.put(giver, receiver);
        }

        return pairs;
    }
}