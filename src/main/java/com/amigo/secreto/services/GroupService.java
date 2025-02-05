package com.amigo.secreto.services;

import com.amigo.secreto.models.Group;
import com.amigo.secreto.models.User;
import com.amigo.secreto.repositories.GroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GroupService {

    private GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
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

    public Group invite(User user, UUID groupId) {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);

        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();
            if (!group.getParticipants().contains(user)) {
                group.getParticipants().add(user);
                return groupRepository.save(group);
            } else {
                throw new RuntimeException("Usuário já está no grupo.");
            }
        } else {
            throw new RuntimeException("Grupo não encontrado.");
        }
    }

}
