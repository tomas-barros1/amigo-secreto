package com.amigo.secreto.services;

import com.amigo.secreto.models.Draw;
import com.amigo.secreto.models.Group;
import com.amigo.secreto.models.User;
import com.amigo.secreto.repositories.DrawRepository;
import com.amigo.secreto.repositories.GroupRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DrawService {

    private DrawRepository drawRepository;
    private GroupRepository groupRepository;

    public DrawService(DrawRepository drawRepository, GroupRepository groupRepository) {
        this.drawRepository = drawRepository;
        this.groupRepository = groupRepository;
    }

    public Draw createDraw(UUID groupId) {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);

        if (optionalGroup.isEmpty())
            throw new RuntimeException("Grupo não encontrado");

        Group group = optionalGroup.get();

        if (group.isAlreadyDrawn())
            throw new RuntimeException("Sorteio já realizado para este grupo.");

        List<User> participants = new ArrayList<>(group.getParticipants());
        Collections.shuffle(participants);

        Map<UUID, UUID> pairs = new HashMap<>();
        for (int i = 0; i < participants.size(); i++) {
            UUID giver = participants.get(i).getId();
            UUID receiver = participants.get((i + 1) % participants.size()).getId();
            pairs.put(giver, receiver);
        }

        Draw draw = new Draw();
        draw.setGroup(group);
        drawRepository.save(draw);

        group.setAlreadyDrawn(true);
        groupRepository.save(group);

        return draw;
    }
}
