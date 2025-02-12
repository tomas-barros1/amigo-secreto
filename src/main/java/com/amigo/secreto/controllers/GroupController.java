package com.amigo.secreto.controllers;

import com.amigo.secreto.dtos.GroupCreateRequestDTO;
import com.amigo.secreto.mappers.GroupMapper;
import com.amigo.secreto.models.Group;
import com.amigo.secreto.services.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<Group> create(@RequestBody GroupCreateRequestDTO dto) {
        Group group = GroupMapper.dtoToGroup(dto);
        return ResponseEntity.ok(groupService.create(group));
    }

    @GetMapping
    public ResponseEntity<List<Group>> findAll() {
        return ResponseEntity.ok(groupService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> find(@PathVariable UUID id) {
        Optional<Group> group = groupService.findById(id);
        return group.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{groupId}/add/{userId}")
    public ResponseEntity<Group> invite(@PathVariable UUID groupId, @PathVariable UUID userId) {
        return ResponseEntity.ok(groupService.invite(userId, groupId));
    }

}
