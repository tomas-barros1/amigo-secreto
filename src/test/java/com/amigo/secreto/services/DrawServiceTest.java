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
        currentUser.setUsername("user@example.com");
        currentUser.setEmail("user@example.com");

        // Create participants (garantindo número par)
        participants = new ArrayList<>();
        for (int i = 0; i < 3; i++) { // Criando 3 participantes + currentUser = 4 (par)
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmail("user" + i + "@example.com");
            user.setUsername("User" + i);
            if (i == 0) {
                user.setWishItem("Item desejado");
            }
            participants.add(user);
        }
        participants.add(currentUser);

        // Create group
        groupId = UUID.randomUUID();
        group = new Group();
        group.setId(groupId);
        group.setOwnerId(currentUser.getId());
        group.setParticipants(participants);

        // Create draw
        drawId = UUID.randomUUID();
        draw = new Draw();
        draw.setId(drawId);
        draw.setGroup(group);
        Map<UUID, UUID> pairs = new HashMap<>();
        draw.setPairs(pairs);

        // Set up repository mocks
        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.of(currentUser));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(drawRepository.findById(drawId)).thenReturn(Optional.of(draw));
    }

    @Test
    void createDraw_Success() {
        // Arrange
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
        assertNotNull(group.getDraw());
        
        // Verificar se não há ninguém tirando a si mesmo
        Map<UUID, UUID> pairs = draw.getPairs();
        for (Map.Entry<UUID, UUID> entry : pairs.entrySet()) {
            assertNotEquals(entry.getKey(), entry.getValue(), "Usuário não pode tirar a si mesmo");
        }
    }

    @Test
    void createDraw_NotGroupOwner() {
        // Arrange
        group.setOwnerId(UUID.randomUUID()); // Muda o dono para outro usuário

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
        group.setDraw(draw); // Simula grupo já sorteado

        // Act & Assert
        DrawAlreadyDoneException exception = assertThrows(
                DrawAlreadyDoneException.class,
                () -> drawService.createDraw(groupId)
        );
        assertEquals("Sorteio já realizado para o grupo de id " + groupId, exception.getMessage());
    }

    @Test
    void getMyFriend_Success() {
        // Arrange
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
    void getMyFriend_DrawNotYetDone() {
        // Arrange
        group.setDraw(null);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> drawService.getMyFriend(groupId)
        );
        assertEquals("O sorteio ainda não foi realizado para este grupo", exception.getMessage());
    }

    @Test
    void getMyFriend_UseFallbackDrawRepository() {
        // Arrange
        group.setDraw(null); // Grupo não tem draw diretamente
        draw.setGroup(group); // Garantir que o draw está associado ao grupo
        group.setDraw(draw); // Garantir que o grupo tem o draw

        User friend = participants.get(0);
        Map<UUID, UUID> pairs = new HashMap<>();
        pairs.put(currentUser.getId(), friend.getId());
        draw.setPairs(pairs);

        when(userRepository.findById(friend.getId())).thenReturn(Optional.of(friend));
        when(drawRepository.findByGroupId(groupId)).thenReturn(Optional.of(draw));

        // Act
        FriendDrawDTO result = drawService.getMyFriend(groupId);

        // Assert
        assertNotNull(result);
        assertEquals(friend.getId(), result.friendId());
        assertEquals(friend.getUsername(), result.friendUsername());
    }
}