package com.amigo.secreto.services;

import com.amigo.secreto.models.Group;
import com.amigo.secreto.models.User;
import com.amigo.secreto.repositories.GroupRepository;
import com.amigo.secreto.repositories.UserRepository;
import com.amigo.secreto.services.exceptions.ResourceNotFoundException;
import com.amigo.secreto.services.exceptions.UserAlreadyInGroupException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupService groupService;

    private User owner;
    private Group group;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setUsername("Alice");
        owner.setEmail("alice@example.com");

        group = new Group();
        group.setId(UUID.randomUUID());
        group.setOwnerId(owner.getId());
        group.setName("Amigo Secreto");
        group.setParticipants(new ArrayList<>());
        group.setDraw(null);
    }

    @Test
    void createShouldReturnGroupWhenOwnerExists() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Retorna o mesmo objeto passado

        Group createdGroup = groupService.create(group);

        assertNotNull(createdGroup);
        assertEquals(group.getId(), createdGroup.getId());
        assertTrue(createdGroup.getParticipants().contains(owner)); // Verifica se o dono foi adicionado aos participantes
        verify(userRepository).findById(owner.getId());
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    void createShouldThrowResourceNotFoundExceptionWhenOwnerDoesNotExist() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> groupService.create(group));
        assertEquals("Usuário de id " + owner.getId() + " não encontrado", exception.getMessage());
        verify(userRepository).findById(owner.getId());
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void findAllShouldReturnListOfGroups() {
        List<Group> groups = Arrays.asList(group);
        when(groupRepository.findAll()).thenReturn(groups);

        List<Group> result = groupService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(group.getId(), result.get(0).getId());
        verify(groupRepository).findAll();
    }

    @Test
    void findByIdShouldReturnGroupWhenGroupExists() {
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        Optional<Group> result = groupService.findById(group.getId());

        assertTrue(result.isPresent());
        assertEquals(group.getId(), result.get().getId());
        verify(groupRepository).findById(group.getId());
    }

    @Test
    void findByIdShouldThrowResourceNotFoundExceptionWhenGroupDoesNotExist() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(groupRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> groupService.findById(nonExistentId).orElseThrow(() -> 
                new ResourceNotFoundException("Grupo de id " + nonExistentId + " não encontrado"))
        );
        
        assertEquals("Grupo de id " + nonExistentId + " não encontrado", exception.getMessage());
        verify(groupRepository).findById(nonExistentId);
    }

    @Test
    void updateShouldReturnUpdatedGroup() {
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Retorna o mesmo objeto passado

        Group updatedGroup = groupService.update(group);

        assertNotNull(updatedGroup);
        assertEquals(group.getId(), updatedGroup.getId());
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    void deleteByIdShouldDeleteGroupWhenGroupExists() {
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        doNothing().when(groupRepository).delete(group);

        groupService.deleteById(group.getId());

        verify(groupRepository).findById(group.getId());
        verify(groupRepository).delete(group);
    }

    @Test
    void deleteByIdShouldThrowResourceNotFoundExceptionWhenGroupDoesNotExist() {
        when(groupRepository.findById(group.getId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> groupService.deleteById(group.getId()));
        assertEquals("Grupo de id " + group.getId() + " não encontrado.", exception.getMessage());
        verify(groupRepository).findById(group.getId());
        verify(groupRepository, never()).delete(any(Group.class));
    }

    @Test
    void inviteShouldAddUserToGroupWhenUserAndGroupExist() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("Bob");
        user.setEmail("bob@example.com");

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Retorna o mesmo objeto passado

        Group updatedGroup = groupService.invite(user.getId(), group.getId());

        assertNotNull(updatedGroup);
        assertTrue(updatedGroup.getParticipants().contains(user)); // Verifica se o usuário foi adicionado ao grupo
        verify(groupRepository).findById(group.getId());
        verify(userRepository).findById(user.getId());
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    void inviteShouldThrowResourceNotFoundExceptionWhenGroupDoesNotExist() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> groupService.invite(userId, groupId));
        assertEquals("Grupo de id " + groupId + " não encontrado.", exception.getMessage());
        verify(groupRepository).findById(groupId);
        verify(userRepository, never()).findById(userId);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void inviteShouldThrowResourceNotFoundExceptionWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> groupService.invite(userId, group.getId()));
        assertEquals("Usuário de id " + userId + " não encontrado.", exception.getMessage());
        verify(groupRepository).findById(group.getId());
        verify(userRepository).findById(userId);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void inviteShouldThrowUserAlreadyInGroupExceptionWhenUserAlreadyInGroup() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("Bob");
        user.setEmail("bob@example.com");

        group.getParticipants().add(user);
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserAlreadyInGroupException exception = assertThrows(UserAlreadyInGroupException.class, () -> groupService.invite(user.getId(), group.getId()));
        assertEquals("Usuário já está no grupo.", exception.getMessage());
        verify(groupRepository).findById(group.getId());
        verify(userRepository).findById(user.getId());
        verify(groupRepository, never()).save(any(Group.class));
    }
}