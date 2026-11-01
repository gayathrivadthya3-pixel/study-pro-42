package com.studypulse.controller;

import com.studypulse.dto.AppreciationDto;
import com.studypulse.service.AppreciationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appreciation")
@CrossOrigin(origins = "*")
public class AppreciationController {

    private final AppreciationService appreciationService;

    public AppreciationController(AppreciationService appreciationService) {
        this.appreciationService = appreciationService;
    }

    @GetMapping("/daily")
    public ResponseEntity<AppreciationDto> getDailyAppreciation(@RequestParam(name = "userId", defaultValue = "1") Long userId) {
        return ResponseEntity.ok(appreciationService.evaluateDailyPerformance(userId));
    }

    @GetMapping("/weekly")
    public ResponseEntity<AppreciationDto> getWeeklyAppreciation(@RequestParam(name = "userId", defaultValue = "1") Long userId) {
        return ResponseEntity.ok(appreciationService.evaluateWeeklyPerformance(userId));
    }
}
