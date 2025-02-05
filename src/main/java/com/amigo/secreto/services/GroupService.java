package com.amigo.secreto.services;

import com.amigo.secreto.models.Group;
import com.amigo.secreto.models.User;
import com.amigo.secreto.repositories.GroupRepository;
import com.amigo.secreto.repositories.UserRepository;
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
        groupRepository.delete(findById(id).get());
    }

    public Group invite(UUID userId, UUID groupId) {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();
            if (!group.getParticipants().contains(optionalUser.get())) {
                group.getParticipants().add(optionalUser.get());
                return groupRepository.save(group);
            } else {
                throw new RuntimeException("Usuário já está no grupo.");
            }
        } else {
            throw new RuntimeException("Grupo não encontrado.");
        }
    }

}
