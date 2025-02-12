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
class DrawServiceTest {

    @Mock
    private DrawRepository drawRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private DrawService drawService;

    private Group group;
    private List<User> participants;
    private Draw draw;

    @BeforeEach
    void setUp() {
        participants = new ArrayList<>();
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setName("Alice");
        user1.setEmail("alice@example.com");
        user1.setPassword("password123");

        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setName("Bob");
        user2.setEmail("bob@example.com");
        user2.setPassword("password456");

        participants.add(user1);
        participants.add(user2);

        group = new Group();
        group.setId(UUID.randomUUID());
        group.setParticipants(participants);
        group.setAlreadyDrawn(false);
        group.setOwnerId(UUID.randomUUID());
        group.setName("Amigo Secreto");

        Map<UUID, UUID> pairs = new HashMap<>();
        pairs.put(participants.get(0).getId(), participants.get(1).getId());
        pairs.put(participants.get(1).getId(), participants.get(0).getId());

        draw = new Draw();
        draw.setId(UUID.randomUUID());
        draw.setGroup(group);
        draw.setPairs(pairs);
    }

    @Test
    void createDrawShouldReturnDrawResponseDTOWhenGroupIsValid() {
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(drawRepository.save(any(Draw.class))).thenAnswer(invocation -> {
            Draw savedDraw = invocation.getArgument(0);
            savedDraw.setId(UUID.randomUUID());
            return savedDraw;
        });

        DrawResponseDTO result = drawService.createDraw(group.getId());

        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(2, result.pairs().size());
        verify(groupRepository, times(1)).findById(group.getId());
        verify(drawRepository, times(1)).save(any(Draw.class));
        verify(groupRepository, times(1)).save(group);
    }

    @Test
    void createDrawShouldThrowResourceNotFoundExceptionWhenGroupDoesNotExist() {
        UUID nonExistentGroupId = UUID.randomUUID();
        when(groupRepository.findById(nonExistentGroupId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> drawService.createDraw(nonExistentGroupId));
        verify(groupRepository, times(1)).findById(nonExistentGroupId);
        verify(drawRepository, never()).save(any(Draw.class));
    }

    @Test
    void createDrawShouldThrowDrawAlreadyDoneExceptionWhenGroupAlreadyDrawn() {
        group.setAlreadyDrawn(true);
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        assertThrows(DrawAlreadyDoneException.class, () -> drawService.createDraw(group.getId()));
        verify(groupRepository, times(1)).findById(group.getId());
        verify(drawRepository, never()).save(any(Draw.class));
    }

    @Test
    void createDrawShouldThrowDrawPairNumberExceptionWhenParticipantsLessThanTwo() {
        User singleUser = new User();
        singleUser.setId(UUID.randomUUID());
        singleUser.setName("Alice");
        singleUser.setEmail("alice@example.com");
        singleUser.setPassword("password123");

        Group invalidGroup = new Group();
        invalidGroup.setId(UUID.randomUUID());
        invalidGroup.setParticipants(Collections.singletonList(singleUser));
        invalidGroup.setAlreadyDrawn(false);
        invalidGroup.setOwnerId(UUID.randomUUID());
        invalidGroup.setName("Invalid Group");

        when(groupRepository.findById(invalidGroup.getId())).thenReturn(Optional.of(invalidGroup));

        assertThrows(DrawPairNumberException.class, () -> drawService.createDraw(invalidGroup.getId()));
        verify(groupRepository, times(1)).findById(invalidGroup.getId());
        verify(drawRepository, never()).save(any(Draw.class));
    }

    @Test
    void createDrawShouldThrowDrawPairNumberExceptionWhenParticipantsNumberIsOdd() {
        User user3 = new User();
        user3.setId(UUID.randomUUID());
        user3.setName("Charlie");
        user3.setEmail("charlie@example.com");
        user3.setPassword("password789");

        participants.add(user3);

        Group invalidGroup = new Group();
        invalidGroup.setId(UUID.randomUUID());
        invalidGroup.setParticipants(participants);
        invalidGroup.setAlreadyDrawn(false);
        invalidGroup.setOwnerId(UUID.randomUUID());
        invalidGroup.setName("Invalid Group");

        when(groupRepository.findById(invalidGroup.getId())).thenReturn(Optional.of(invalidGroup));

        assertThrows(DrawPairNumberException.class, () -> drawService.createDraw(invalidGroup.getId()));
        verify(groupRepository, times(1)).findById(invalidGroup.getId());
        verify(drawRepository, never()).save(any(Draw.class));
    }

    @Test
    void getDraw_ShouldReturnDrawResponseDTO_WhenDrawExists() {
        when(drawRepository.findById(draw.getId())).thenReturn(Optional.of(draw));

        DrawResponseDTO result = drawService.getDraw(draw.getId());

        assertNotNull(result);
        assertEquals(draw.getId(), result.id());
        assertEquals(2, result.pairs().size());
        verify(drawRepository, times(1)).findById(draw.getId());
    }

    @Test
    void getDraw_ShouldThrowResourceNotFoundException_WhenDrawDoesNotExist() {
        UUID nonExistentDrawId = UUID.randomUUID();
        when(drawRepository.findById(nonExistentDrawId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> drawService.getDraw(nonExistentDrawId));
        verify(drawRepository, times(1)).findById(nonExistentDrawId);
    }
}