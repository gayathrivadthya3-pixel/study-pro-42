package com.studypulse.controller;

import com.studypulse.dto.StudyTimingDto;
import com.studypulse.service.StudyTimingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timings")
@CrossOrigin(origins = "*")
public class StudyTimingController {

    private final StudyTimingService studyTimingService;

    public StudyTimingController(StudyTimingService studyTimingService) {
        this.studyTimingService = studyTimingService;
    }

    @GetMapping
    public ResponseEntity<List<StudyTimingDto>> getTimings(
            @RequestParam(name = "userId", defaultValue = "1") Long userId,
            @RequestParam(name = "category", required = false) String category) {
        return ResponseEntity.ok(studyTimingService.getTimings(userId, category));
    }

    @PostMapping
    public ResponseEntity<StudyTimingDto> createTiming(
            @RequestParam(name = "userId", defaultValue = "1") Long userId,
            @RequestBody StudyTimingDto dto) {
        return ResponseEntity.ok(studyTimingService.createTiming(userId, dto));
    }

    @PutMapping("/{timingId}")
    public ResponseEntity<StudyTimingDto> updateTiming(
            @RequestParam(name = "userId", defaultValue = "1") Long userId,
            @PathVariable(name = "timingId") Long timingId,
            @RequestBody StudyTimingDto dto) {
        return ResponseEntity.ok(studyTimingService.updateTiming(userId, timingId, dto));
    }

    @DeleteMapping("/{timingId}")
    public ResponseEntity<?> deleteTiming(
            @RequestParam(name = "userId", defaultValue = "1") Long userId,
            @PathVariable(name = "timingId") Long timingId) {
        studyTimingService.deleteTiming(userId, timingId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Study timing deleted successfully"));
    }
}
