package com.amigo.secreto.controllers;

import com.amigo.secreto.controllers.exceptions.BaseException;
import com.amigo.secreto.dtos.DrawResponseDTO;
import com.amigo.secreto.dtos.FriendDrawDTO;
import com.amigo.secreto.services.DrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/draws")
@Tag(name = "Sorteios", description = "Endpoints para gerenciar sorteios")
public class DrawController {

    private final DrawService drawService;

    public DrawController(DrawService drawService) {
        this.drawService = drawService;
    }

    @PostMapping("/create")
    @Operation(summary = "Criar sorteio (número de membros do grupo deve ser par e não pode ser sorteado anteriormente e apenas dono pode sortear)", description = "Cria um sorteio para o grupo especificado. Apenas o dono do grupo pode criar o sorteio.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sorteio criado com sucesso",
                    content = @Content(schema = @Schema(implementation = DrawResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido - Apenas o dono do grupo pode criar o sorteio",
                    content = @Content(schema = @Schema(implementation = BaseException.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado",
                    content = @Content(schema = @Schema(implementation = BaseException.class))),
            @ApiResponse(responseCode = "409", description = "Sorteio já realizado ou número inválido de participantes",
                    content = @Content(schema = @Schema(implementation = BaseException.class)))
    })
    public ResponseEntity<DrawResponseDTO> createDraw(@RequestParam UUID groupId) {
        DrawResponseDTO drawDTO = drawService.createDraw(groupId);
        return ResponseEntity.ok(drawDTO);
    }

    @GetMapping("/{drawId}")
    @Operation(summary = "Obter sorteio por ID", description = "Retorna os detalhes de um sorteio específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sorteio encontrado",
                    content = @Content(schema = @Schema(implementation = DrawResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Sorteio não encontrado",
                    content = @Content(schema = @Schema(implementation = BaseException.class)))
    })
    public ResponseEntity<DrawResponseDTO> getDraw(@PathVariable UUID drawId) {
        DrawResponseDTO drawDTO = drawService.getDraw(drawId);
        return ResponseEntity.ok(drawDTO);
    }

    @GetMapping("/my-friend")
    @Operation(summary = "Ver meu amigo sorteado",
            description = "Retorna o amigo que o usuário atual tirou no sorteio e o item que ele deseja.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informações do amigo sorteado encontradas",
                    content = @Content(schema = @Schema(implementation = FriendDrawDTO.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado ou sorteio não realizado ou usuário não é participante",
                    content = @Content(schema = @Schema(implementation = BaseException.class)))
    })
    public ResponseEntity<FriendDrawDTO> getMyFriend(@RequestParam UUID groupId) {
        FriendDrawDTO friendDrawDTO = drawService.getMyFriend(groupId);
        return ResponseEntity.ok(friendDrawDTO);
    }
}