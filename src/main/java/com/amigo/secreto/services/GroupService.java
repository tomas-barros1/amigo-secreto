package com.amigo.secreto.services;

import com.amigo.secreto.models.Group;
import com.amigo.secreto.models.User;
import com.amigo.secreto.repositories.GroupRepository;
import com.amigo.secreto.repositories.UserRepository;
import com.amigo.secreto.services.exceptions.ResourceNotFoundException;
import com.amigo.secreto.services.exceptions.UserAlreadyInGroupException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GroupService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public Group create(Group group) {
        User owner = userRepository.findById(group.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário de id " + group.getOwnerId() + " não encontrado"));

        group.getParticipants().add(owner);

        return groupRepository.save(group);
    }

    public List<Group> findAll() {
        return groupRepository.findAll();
    }

    public Optional<Group> findById(UUID id) {
        return groupRepository.findById(id);
    }

    public Group update(Group group) {
        return groupRepository.save(group);
    }

    public void deleteById(UUID id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo de id " + id + " não encontrado."));
        groupRepository.delete(group);
    }

    public Group invite(UUID userId, UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo de id " + groupId + " não encontrado."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário de id " + userId + " não encontrado."));

        if (group.getParticipants().contains(user)) {
            throw new UserAlreadyInGroupException("Usuário já está no grupo.");
        }

        group.getParticipants().add(user);
        return groupRepository.save(group);
    }

}
