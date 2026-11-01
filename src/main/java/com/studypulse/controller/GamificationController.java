package com.studypulse.controller;

import com.studypulse.dto.GamifiedProgressDto;
import com.studypulse.service.GamificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gamification")
@CrossOrigin(origins = "*")
public class GamificationController {

    private final GamificationService gamificationService;

    public GamificationController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @GetMapping("/progress")
    public ResponseEntity<GamifiedProgressDto> getProgress(@RequestParam(name = "userId", defaultValue = "1") Long userId) {
        return ResponseEntity.ok(gamificationService.getProgress(userId));
    }
}
