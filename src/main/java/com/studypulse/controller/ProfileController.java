package com.studypulse.controller;

import com.studypulse.model.User;
import com.studypulse.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(@RequestParam(name = "userId", defaultValue = "1") Long userId) {
        return userRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestParam(name = "userId", defaultValue = "1") Long userId,
            @RequestBody Map<String, String> payload) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (payload.containsKey("name") && !payload.get("name").isBlank()) {
            user.setName(payload.get("name"));
        }
        if (payload.containsKey("username") && !payload.get("username").isBlank()) {
            String newUsername = payload.get("username").trim();
            if (!newUsername.equalsIgnoreCase(user.getUsername())) {
                if (userRepository.existsByUsernameIgnoreCase(newUsername)) {
                    throw new IllegalArgumentException("Username '" + newUsername + "' is already taken.");
                }
                user.setUsername(newUsername);
            }
        }
        if (payload.containsKey("password") && !payload.get("password").isBlank()) {
            user.setPassword(payload.get("password").trim());
        }
        if (payload.containsKey("avatar") && !payload.get("avatar").isBlank()) {
            user.setAvatar(payload.get("avatar"));
        }
        if (payload.containsKey("targetSemesterExam")) {
            user.setTargetSemesterExam(payload.get("targetSemesterExam"));
        }
        if (payload.containsKey("targetGateYear")) {
            user.setTargetGateYear(payload.get("targetGateYear"));
        }
        if (payload.containsKey("targetLanguage")) {
            user.setTargetLanguage(payload.get("targetLanguage"));
        }
        if (payload.containsKey("targetInterviewRole")) {
            user.setTargetInterviewRole(payload.get("targetInterviewRole"));
        }

        User updated = userRepository.save(user);
        return ResponseEntity.ok(updated);
    }
}
