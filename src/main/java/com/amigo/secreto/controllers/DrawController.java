package com.amigo.secreto.controllers;

import com.amigo.secreto.models.Draw;
import com.amigo.secreto.services.DrawService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Draw> createDraw(@RequestParam UUID groupId) {
        Draw draw = drawService.createDraw(groupId);
        return ResponseEntity.ok(draw);
    }

    @GetMapping("/{drawId}")
    public ResponseEntity<Draw> getDraw(@PathVariable UUID drawId) {
        return drawService.getDraw(drawId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
