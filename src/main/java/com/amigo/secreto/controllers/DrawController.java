package com.amigo.secreto.controllers;

import com.amigo.secreto.dtos.DrawResponseDTO;
import com.amigo.secreto.models.Draw;
import com.amigo.secreto.services.DrawService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
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
        Draw draw = drawService.createDraw(groupId);
        DrawResponseDTO drawDTO = drawService.getDraw(draw.getId()).orElseThrow(
                () -> new RuntimeException("Erro ao criar sorteio")
        );
        return ResponseEntity.ok(drawDTO);
    }

    @GetMapping("/{drawId}")
    public ResponseEntity<DrawResponseDTO> getDraw(@PathVariable UUID drawId) {
        Optional<DrawResponseDTO> drawDTO = drawService.getDraw(drawId);
        return drawDTO.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

}
