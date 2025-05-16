package com.amigo.secreto.services;

import com.amigo.secreto.dtos.DrawResponseDTO;
import com.amigo.secreto.dtos.FriendDrawDTO;
import com.amigo.secreto.mappers.DrawMapper;
import com.amigo.secreto.models.Draw;
import com.amigo.secreto.models.Group;
import com.amigo.secreto.models.User;
import com.amigo.secreto.repositories.DrawRepository;
import com.amigo.secreto.repositories.GroupRepository;
import com.amigo.secreto.repositories.UserRepository;
import com.amigo.secreto.services.exceptions.DrawAlreadyDoneException;
import com.amigo.secreto.services.exceptions.DrawPairNumberException;
import com.amigo.secreto.services.exceptions.ForbiddenException;
import com.amigo.secreto.services.exceptions.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DrawService {
    private final DrawRepository drawRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public DrawService(DrawRepository drawRepository, GroupRepository groupRepository, UserRepository userRepository) {
        this.drawRepository = drawRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public DrawResponseDTO createDraw(UUID groupId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo de id " + groupId + " não encontrado"));

        if (!group.getOwnerId().equals(currentUser.getId())) {
            throw new ForbiddenException("Apenas o criador do grupo pode realizar o sorteio");
        }

        validateDrawConditions(group);

        Map<UUID, UUID> pairs = generatePairs(group.getParticipants());

        Draw draw = new Draw();
        draw.setGroup(group);
        draw.setPairs(pairs);
        drawRepository.save(draw);

        group.setAlreadyDrawn(true);
        group.setDraw(draw);
        groupRepository.save(group);

        return DrawMapper.toDrawResponseDTO(draw);
    }

    public DrawResponseDTO getDraw(UUID drawId) {
        Draw draw = drawRepository.findById(drawId)
                .orElseThrow(() -> new ResourceNotFoundException("Sorteio de id " + drawId + " não encontrado"));
        return DrawMapper.toDrawResponseDTO(draw);
    }

    public FriendDrawDTO getMyFriend(UUID groupId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo de id " + groupId + " não encontrado"));

        if (!group.isAlreadyDrawn()) {
            throw new ResourceNotFoundException("O sorteio ainda não foi realizado para este grupo");
        }

        boolean isParticipant = group.getParticipants().stream()
                .anyMatch(participant -> participant.getId().equals(currentUser.getId()));
        if (!isParticipant) {
            throw new ResourceNotFoundException("Você não é participante deste grupo");
        }

        Draw draw = group.getDraw();
        if (draw == null) {
            draw = drawRepository.findByGroupId(groupId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sorteio não encontrado para o grupo " + groupId));
        }

        Map<UUID, UUID> pairs = draw.getPairs();
        UUID friendId = pairs.get(currentUser.getId());

        if (friendId == null) {
            throw new ResourceNotFoundException("Você não possui um amigo sorteado neste grupo");
        }

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new ResourceNotFoundException("Amigo sorteado não encontrado"));

        return new FriendDrawDTO(
                friend.getId(),
                friend.getUsername(),
                friend.getWishItem() != null ? friend.getWishItem() : "Nenhum item desejado cadastrado"
        );
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