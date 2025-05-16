package com.amigo.secreto.controllers;

import com.amigo.secreto.models.User;
import com.amigo.secreto.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuários", description = "Endpoints para gerenciar usuários")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Obter todos os usuários", description = "Retorna a lista de todos os usuários cadastrados.")
    @ApiResponse(responseCode = "200", description = "Usuários encontrados com sucesso")
    public ResponseEntity<List<User>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna os detalhes de um usuário baseado no ID fornecido.")
    @ApiResponse(responseCode = "200", description = "Usuário encontrado")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    public ResponseEntity<User> findById(@PathVariable UUID id) {
        Optional<User> user = userService.findById(id);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar usuário", description = "Deleta o usuário com o ID fornecido.")
    @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/participating-groups")
    @Operation(summary = "Ver quantos grupos usuário logado está participando", description = "Ver quantos grupos usuário logado está participando")
    @ApiResponse(responseCode = "204", description = "Retorna número de grupos que o usuário participa")
    @ApiResponse(responseCode = "404", description = "Erro")
    public ResponseEntity<Integer> participatingGroups() {
        return ResponseEntity.ok(userService.participatingGroups());
    }

    @GetMapping("/participating-draws")
    @Operation(
            summary = "Ver quantos sorteios o usuário logado já participou",
            description = "Retorna o número de grupos nos quais o sorteio já foi realizado e o usuário está participando"
    )
    @ApiResponse(responseCode = "200", description = "Retorna número de sorteios dos quais o usuário participa")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    public ResponseEntity<Integer> participatingDraws() {
        return ResponseEntity.ok(userService.participatingDraws());
    }

}
