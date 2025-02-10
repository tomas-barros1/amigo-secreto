package com.amigo.secreto.controllers;

import com.amigo.secreto.dtos.DrawResponseDTO;
import com.amigo.secreto.services.DrawService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/draws")
public class DrawController {

    private final DrawService drawService;

    public DrawController(DrawService drawService) {
        this.drawService = drawService;
    }

    @PostMapping("/create")
    public ResponseEntity<DrawResponseDTO> createDraw(@RequestParam UUID groupId) {
        DrawResponseDTO drawDTO = drawService.createDraw(groupId);
        return ResponseEntity.ok(drawDTO);
    }

    @GetMapping("/{drawId}")
    public ResponseEntity<DrawResponseDTO> getDraw(@PathVariable UUID drawId) {
        DrawResponseDTO drawDTO = drawService.getDraw(drawId);
        return ResponseEntity.ok(drawDTO);
    }

}
