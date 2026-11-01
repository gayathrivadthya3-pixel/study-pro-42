package com.studypulse.controller;

import com.studypulse.dto.TaskDto;
import com.studypulse.service.TaskService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasks(
            @RequestParam(name = "userId", defaultValue = "1") Long userId,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "category", required = false) String category) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(taskService.getTasks(userId, queryDate, category));
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            @RequestParam(name = "userId", defaultValue = "1") Long userId,
            @RequestBody TaskDto dto) {
        return ResponseEntity.ok(taskService.createTask(userId, dto));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDto> updateTask(
            @RequestParam(name = "userId", defaultValue = "1") Long userId,
            @PathVariable(name = "taskId") Long taskId,
            @RequestBody TaskDto dto) {
        return ResponseEntity.ok(taskService.updateTask(userId, taskId, dto));
    }

    @PostMapping("/{taskId}/toggle")
    public ResponseEntity<TaskDto> toggleTask(
            @RequestParam(name = "userId", defaultValue = "1") Long userId,
            @PathVariable(name = "taskId") Long taskId) {
        return ResponseEntity.ok(taskService.toggleTaskCompletion(userId, taskId));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(
            @RequestParam(name = "userId", defaultValue = "1") Long userId,
            @PathVariable(name = "taskId") Long taskId) {
        taskService.deleteTask(userId, taskId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Task deleted successfully"));
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<?> clearAllTasks(
            @RequestParam(name = "userId", defaultValue = "1") Long userId) {
        taskService.clearAllTasks(userId);
        return ResponseEntity.ok(Map.of("success", true, "message", "All tasks cleared successfully"));
    }

    @PostMapping("/default")
    public ResponseEntity<List<TaskDto>> loadDefaultTasks(
            @RequestParam(name = "userId", defaultValue = "1") Long userId,
            @RequestParam(name = "category", defaultValue = "ALL") String category,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        List<TaskDto> loaded = taskService.loadDefaultTasks(userId, category, targetDate);
        return ResponseEntity.ok(loaded);
    }
}
