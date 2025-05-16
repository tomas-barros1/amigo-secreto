package com.amigo.secreto.controllers;

import com.amigo.secreto.dtos.GroupCreateRequestDTO;
import com.amigo.secreto.mappers.GroupMapper;
import com.amigo.secreto.models.Group;
import com.amigo.secreto.services.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/groups")
@Tag(name = "Grupos", description = "Endpoints para gerenciar grupos")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    @Operation(summary = "Criar grupo", description = "Cria um novo grupo.")
    @ApiResponse(responseCode = "200", description = "Grupo criado com sucesso")
    public ResponseEntity<Group> create(@RequestBody GroupCreateRequestDTO dto) {
        Group group = GroupMapper.dtoToGroup(dto);
        return ResponseEntity.ok(groupService.create(group));
    }

    @GetMapping
    @Operation(summary = "Obter todos os grupos", description = "Retorna a lista de todos os grupos.")
    @ApiResponse(responseCode = "200", description = "Grupos encontrados com sucesso")
    public ResponseEntity<List<Group>> findAll() {
        return ResponseEntity.ok(groupService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar grupo por ID", description = "Retorna os detalhes de um grupo baseado no ID fornecido.")
    @ApiResponse(responseCode = "200", description = "Grupo encontrado")
    @ApiResponse(responseCode = "404", description = "Grupo não encontrado")
    public ResponseEntity<Group> find(@PathVariable UUID id) {
        Optional<Group> group = groupService.findById(id);
        return group.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{groupId}/add/{userId}")
    @Operation(summary = "Adicionar usuário ao grupo", description = "Adiciona um usuário a um grupo existente.")
    @ApiResponse(responseCode = "200", description = "Usuário adicionado ao grupo com sucesso")
    public ResponseEntity<Group> invite(
            @PathVariable UUID groupId,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(groupService.invite(userId, groupId));
    }
}