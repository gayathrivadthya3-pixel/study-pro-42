package com.studypulse.controller;

import com.studypulse.dto.*;
import com.studypulse.model.User;
import com.studypulse.model.UserProgress;
import com.studypulse.service.AuthService;
import com.studypulse.service.GamificationService;
import com.studypulse.service.SmsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final GamificationService gamificationService;

    public AuthController(AuthService authService, GamificationService gamificationService) {
        this.authService = authService;
        this.gamificationService = gamificationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserAuthDto request) {
        try {
            AuthResponseDto response = authService.registerWithUsernamePassword(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserAuthDto request) {
        try {
            AuthResponseDto response = authService.loginWithUsernamePassword(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/credentials")
    public ResponseEntity<?> updateCredentials(
            @RequestParam(name = "userId", defaultValue = "1") Long userId,
            @RequestBody UserAuthDto request) {
        try {
            User updated = authService.updateCredentials(userId, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Username and password updated successfully!",
                    "user", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody OtpRequestDto request) {
        try {
            if (request.getMobileNumber() == null || request.getMobileNumber().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mobile number is required"));
            }
            SmsService.SmsDeliveryResult delivery = authService.generateAndSendOtp(request.getMobileNumber());
            Map<String, Object> response = new HashMap<>();
            response.put("success", delivery.isSuccess());
            response.put("liveGatewayUsed", delivery.isLiveGatewayUsed());
            response.put("message", delivery.getMessage());
            if (!delivery.isLiveGatewayUsed()) {
                response.put("testOtpCode", delivery.getOtpCode());
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/sms-key")
    public ResponseEntity<?> setFast2SmsKey(@RequestBody Map<String, String> body) {
        String key = body.get("apiKey");
        authService.getSmsService().setFast2smsApiKey(key);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Fast2SMS API key configured! Live cellular SMS will now be sent to mobile phones."
        ));
    }

    @GetMapping("/sms-status")
    public ResponseEntity<?> getSmsStatus() {
        return ResponseEntity.ok(Map.of(
                "liveGatewayConfigured", authService.getSmsService().isLiveConfigured()
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyDto request) {
        try {
            AuthResponseDto response = authService.verifyOtpAndLogin(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/guest")
    public ResponseEntity<?> loginGuest() {
        try {
            AuthResponseDto response = authService.loginAsGuest();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestParam(name = "userId", defaultValue = "1") Long userId) {
        try {
            User user = authService.getUserById(userId);
            UserProgress progress = authService.initUserProgressIfAbsent(user);
            return ResponseEntity.ok(new AuthResponseDto("active_session", "User authenticated", user, progress));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
