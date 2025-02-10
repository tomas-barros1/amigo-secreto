package com.amigo.secreto.services;

import com.amigo.secreto.models.Group;
import com.amigo.secreto.models.User;
import com.amigo.secreto.repositories.GroupRepository;
import com.amigo.secreto.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @InjectMocks
    private GroupService groupService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("Should create a group successfully")
    void createGroupSuccessfully() {
        UUID ownerID = UUID.randomUUID();
        UUID groupID = UUID.randomUUID();

        User user = new User(ownerID, "Marcos", "marcos@gmail.com", "ahisdhi123", "Iphone 15", null);

        Group group = new Group(groupID, "grupo", ownerID, false, LocalDateTime.now(), null, null);

        when(userRepository.findById(ownerID)).thenReturn(Optional.of(user));
        when(groupRepository.save(group)).thenReturn(group);

        Group createdGroup = groupService.create(group);

        assertNotNull(createdGroup); // Ensure the group is not null
        assertEquals("grupo", createdGroup.getName()); // Verify group name
        assertTrue(createdGroup.getParticipants().contains(user)); // Verify that the user was added to participants
        assertEquals(ownerID, createdGroup.getOwnerId()); // Verify that the owner id is correctly set

        verify(userRepository, times(1)).findById(ownerID);
        verify(groupRepository, times(1)).save(group); // Check if the groupRepository was called once
    }




}