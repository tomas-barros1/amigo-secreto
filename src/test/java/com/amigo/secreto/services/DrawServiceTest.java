package com.amigo.secreto.services;

import com.amigo.secreto.dtos.DrawResponseDTO;
import com.amigo.secreto.dtos.FriendDrawDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DrawServiceTest {

    @Mock
    private DrawRepository drawRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private DrawService drawService;

    private User currentUser;
    private Group group;
    private List<User> participants;
    private Draw draw;
    private UUID groupId;
    private UUID drawId;

    @BeforeEach
    void setUp() {
        // Configure SecurityContextHolder
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("user@example.com");

        // Create test users
        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        currentUser.setEmail("user@example.com");
        currentUser.setUsername("TestUser");

        // Create participants
        participants = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmail("user" + i + "@example.com");
            user.setUsername("User" + i);
            if (i == 0) {
                user.setWishItem("Item desejado");
            }
            participants.add(user);
        }

        // Add current user to participants
        participants.add(currentUser);

        // Create group
        groupId = UUID.randomUUID();
        group = new Group();
        group.setId(groupId);
        group.setOwnerId(currentUser.getId());
        group.setParticipants(participants);
        group.setAlreadyDrawn(false);

        // Create draw
        drawId = UUID.randomUUID();
        draw = new Draw();
        draw.setId(drawId);
        draw.setGroup(group);

        Map<UUID, UUID> pairs = new HashMap<>();
        draw.setPairs(pairs);

        // Set up repository mocks
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(currentUser));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(drawRepository.findById(drawId)).thenReturn(Optional.of(draw));
    }

    @Test
    void createDraw_Success() {
        // Arrange
        // Garantir número par de participantes (remover um se for ímpar)
        if (participants.size() % 2 != 0) {
            participants.remove(participants.size() - 1);
            group.setParticipants(participants);
        }

        when(drawRepository.save(any(Draw.class))).thenAnswer(invocation -> {
            Draw savedDraw = invocation.getArgument(0);
            savedDraw.setId(drawId);
            return savedDraw;
        });

        // Act
        DrawResponseDTO result = drawService.createDraw(groupId);

        // Assert
        assertNotNull(result);
        assertEquals(drawId, result.id());
        verify(drawRepository).save(any(Draw.class));
        verify(groupRepository).save(group);
        assertTrue(group.isAlreadyDrawn());
        assertNotNull(group.getDraw());
    }

    @Test
    void createDraw_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> drawService.createDraw(groupId)
        );
        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void createDraw_GroupNotFound() {
        // Arrange
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> drawService.createDraw(groupId)
        );
        assertEquals("Grupo de id " + groupId + " não encontrado", exception.getMessage());
    }

    @Test
    void createDraw_NotGroupOwner() {
        // Arrange
        group.setOwnerId(UUID.randomUUID()); // Change owner to someone else

        // Act & Assert
        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> drawService.createDraw(groupId)
        );
        assertEquals("Apenas o criador do grupo pode realizar o sorteio", exception.getMessage());
    }

    @Test
    void createDraw_AlreadyDrawn() {
        // Arrange
        group.setAlreadyDrawn(true);

        // Act & Assert
        DrawAlreadyDoneException exception = assertThrows(
                DrawAlreadyDoneException.class,
                () -> drawService.createDraw(groupId)
        );
        assertEquals("Sorteio já realizado para o grupo de id " + groupId, exception.getMessage());
    }

    @Test
    void createDraw_InsufficientParticipants() {
        // Arrange
        group.setParticipants(Collections.singletonList(currentUser));

        // Act & Assert
        DrawPairNumberException exception = assertThrows(
                DrawPairNumberException.class,
                () -> drawService.createDraw(groupId)
        );
        assertEquals("O grupo precisa ter pelo menos 2 participantes para realizar o sorteio.", exception.getMessage());
    }

    @Test
    void createDraw_OddNumberOfParticipants() {
        // Arrange
        List<User> oddParticipants = new ArrayList<>(participants.subList(0, 3));
        group.setParticipants(oddParticipants);

        // Act & Assert
        DrawPairNumberException exception = assertThrows(
                DrawPairNumberException.class,
                () -> drawService.createDraw(groupId)
        );
        assertEquals("Número de participantes precisa ser par", exception.getMessage());
    }

    @Test
    void getDraw_Success() {
        // Act
        DrawResponseDTO result = drawService.getDraw(drawId);

        // Assert
        assertNotNull(result);
        assertEquals(drawId, result.id());
    }

    @Test
    void getDraw_NotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(drawRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> drawService.getDraw(nonExistentId)
        );
        assertEquals("Sorteio de id " + nonExistentId + " não encontrado", exception.getMessage());
    }

    @Test
    void getMyFriend_Success() {
        // Arrange
        group.setAlreadyDrawn(true);
        group.setDraw(draw);

        User friend = participants.get(0);
        Map<UUID, UUID> pairs = new HashMap<>();
        pairs.put(currentUser.getId(), friend.getId());
        draw.setPairs(pairs);

        when(userRepository.findById(friend.getId())).thenReturn(Optional.of(friend));

        // Act
        FriendDrawDTO result = drawService.getMyFriend(groupId);

        // Assert
        assertNotNull(result);
        assertEquals(friend.getId(), result.friendId());
        assertEquals(friend.getUsername(), result.friendUsername());
        assertEquals(friend.getWishItem(), result.wishItem());
    }

    @Test
    void getMyFriend_NoWishItem() {
        // Arrange
        group.setAlreadyDrawn(true);
        group.setDraw(draw);

        // Find a user without wishItem
        User friendWithoutWishItem = participants.get(1); // index 1 should not have wishItem set

        // Set up the pairs
        Map<UUID, UUID> pairs = new HashMap<>();
        pairs.put(currentUser.getId(), friendWithoutWishItem.getId());
        draw.setPairs(pairs);

        when(userRepository.findById(friendWithoutWishItem.getId())).thenReturn(Optional.of(friendWithoutWishItem));

        // Act
        FriendDrawDTO result = drawService.getMyFriend(groupId);

        // Assert
        assertNotNull(result);
        assertEquals(friendWithoutWishItem.getId(), result.friendId());
        assertEquals(friendWithoutWishItem.getUsername(), result.friendUsername());
        assertEquals("Nenhum item desejado cadastrado", result.wishItem());
    }

    @Test
    void getMyFriend_DrawNotYetDone() {
        // Arrange
        group.setAlreadyDrawn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> drawService.getMyFriend(groupId)
        );
        assertEquals("O sorteio ainda não foi realizado para este grupo", exception.getMessage());
    }

    @Test
    void getMyFriend_UserNotParticipant() {
        // Arrange
        group.setAlreadyDrawn(true);
        group.setParticipants(participants.subList(0, 4)); // Remove current user from participants

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> drawService.getMyFriend(groupId)
        );
        assertEquals("Você não é participante deste grupo", exception.getMessage());
    }

    @Test
    void getMyFriend_DrawNotInGroup() {
        // Arrange
        group.setAlreadyDrawn(true);
        group.setDraw(null);
        when(drawRepository.findByGroupId(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> drawService.getMyFriend(groupId)
        );
        assertEquals("Sorteio não encontrado para o grupo " + groupId, exception.getMessage());
    }

    @Test
    void getMyFriend_FriendNotFound() {
        // Arrange
        group.setAlreadyDrawn(true);
        group.setDraw(draw);

        // Current user has no paired friend
        Map<UUID, UUID> pairs = new HashMap<>();
        draw.setPairs(pairs);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> drawService.getMyFriend(groupId)
        );
        assertEquals("Você não possui um amigo sorteado neste grupo", exception.getMessage());
    }

    @Test
    void getMyFriend_FriendUserNotFound() {
        // Arrange
        group.setAlreadyDrawn(true);
        group.setDraw(draw);

        UUID friendId = UUID.randomUUID();
        Map<UUID, UUID> pairs = new HashMap<>();
        pairs.put(currentUser.getId(), friendId);
        draw.setPairs(pairs);

        when(userRepository.findById(friendId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> drawService.getMyFriend(groupId)
        );
        assertEquals("Amigo sorteado não encontrado", exception.getMessage());
    }

    @Test
    void getMyFriend_UseFallbackDrawRepository() {
        // Arrange
        group.setAlreadyDrawn(true);
        group.setDraw(null); // Group doesn't have draw directly

        when(drawRepository.findByGroupId(groupId)).thenReturn(Optional.of(draw));

        User friend = participants.get(0);
        Map<UUID, UUID> pairs = new HashMap<>();
        pairs.put(currentUser.getId(), friend.getId());
        draw.setPairs(pairs);

        when(userRepository.findById(friend.getId())).thenReturn(Optional.of(friend));

        // Act
        FriendDrawDTO result = drawService.getMyFriend(groupId);

        // Assert
        assertNotNull(result);
        verify(drawRepository).findByGroupId(groupId);
    }
}