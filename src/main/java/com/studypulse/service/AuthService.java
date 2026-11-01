package com.studypulse.service;

import com.studypulse.dto.*;
import com.studypulse.model.OtpVerification;
import com.studypulse.model.User;
import com.studypulse.model.UserProgress;
import com.studypulse.repository.OtpVerificationRepository;
import com.studypulse.repository.UserProgressRepository;
import com.studypulse.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final StudyTimingService studyTimingService;
    private final TaskService taskService;
    private final SmsService smsService;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository userRepository,
                       UserProgressRepository userProgressRepository,
                       OtpVerificationRepository otpVerificationRepository,
                       StudyTimingService studyTimingService,
                       TaskService taskService,
                       SmsService smsService) {
        this.userRepository = userRepository;
        this.userProgressRepository = userProgressRepository;
        this.otpVerificationRepository = otpVerificationRepository;
        this.studyTimingService = studyTimingService;
        this.taskService = taskService;
        this.smsService = smsService;
    }

    /**
     * Generate 6-digit OTP and deliver it directly to the mobile number via SMS
     */
    @Transactional
    public SmsService.SmsDeliveryResult generateAndSendOtp(String mobileNumber) {
        // Clean mobile number (strip spaces/dashes)
        String cleanedNumber = mobileNumber.replaceAll("[^0-9+]", "");
        
        // Generate random 6-digit numeric OTP
        int codeInt = 100000 + random.nextInt(900000);
        String otpCode = String.valueOf(codeInt);

        // Save OTP (valid for 10 minutes)
        OtpVerification otpVerification = new OtpVerification(cleanedNumber, otpCode, 10);
        otpVerificationRepository.save(otpVerification);

        // Deliver SMS to the mobile phone
        return smsService.sendOtpSms(cleanedNumber, otpCode);
    }

    /**
     * Verify OTP and Login / Register User
     */
    @Transactional
    public AuthResponseDto verifyOtpAndLogin(OtpVerifyDto dto) {
        String cleanedNumber = dto.getMobileNumber().replaceAll("[^0-9+]", "");
        
        Optional<OtpVerification> otpOpt = otpVerificationRepository
                .findTopByMobileNumberAndVerifiedFalseOrderByCreatedAtDesc(cleanedNumber);

        if (otpOpt.isEmpty()) {
            throw new IllegalArgumentException("No active OTP found for this mobile number. Please click 'Generate OTP'.");
        }

        OtpVerification otp = otpOpt.get();
        if (otp.isExpired()) {
            throw new IllegalArgumentException("OTP has expired. Please request a new OTP.");
        }

        // Check OTP entered against database code (or demo fallback 123456)
        if (!otp.getOtpCode().equals(dto.getOtpCode().trim()) && !"123456".equals(dto.getOtpCode().trim())) {
            throw new IllegalArgumentException("Invalid OTP code. Please check your SMS messages and try again.");
        }

        // Mark OTP verified
        otp.setVerified(true);
        otpVerificationRepository.save(otp);

        // Find or create User
        User user = userRepository.findByMobileNumber(cleanedNumber).orElseGet(() -> {
            User newUser = new User();
            newUser.setMobileNumber(cleanedNumber);
            String displayName = (dto.getName() != null && !dto.getName().isBlank())
                    ? dto.getName()
                    : "Student " + (cleanedNumber.length() >= 4 ? cleanedNumber.substring(cleanedNumber.length() - 4) : "User");
            newUser.setName(displayName);
            newUser.setAvatar("https://api.dicebear.com/7.x/bottts/svg?seed=" + cleanedNumber);
            newUser.setAuthProvider("MOBILE_OTP");
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setLastLoginAt(LocalDateTime.now());
            return userRepository.save(newUser);
        });

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Ensure progress and default schedules exist
        UserProgress progress = initUserProgressIfAbsent(user);
        studyTimingService.initDefaultTimingsIfAbsent(user.getId());
        taskService.initDefaultTasksIfAbsent(user.getId());

        String dummyToken = "jwt_mob_" + user.getId() + "_" + System.currentTimeMillis();
        return new AuthResponseDto(dummyToken, "Login successful via Mobile OTP!", user, progress);
    }

    /**
     * Guest / Fast Login for instant access
     */
    @Transactional
    public AuthResponseDto loginAsGuest() {
        String guestId = "guest_" + System.currentTimeMillis();
        User user = new User();
        user.setName("Scholar Guest");
        user.setMobileNumber("+91-DEMO-STUDENT");
        user.setEmail(guestId + "@studypulse.local");
        user.setAvatar("https://api.dicebear.com/7.x/bottts/svg?seed=" + guestId);
        user.setAuthProvider("GUEST");
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLoginAt(LocalDateTime.now());
        user = userRepository.save(user);

        UserProgress progress = initUserProgressIfAbsent(user);
        studyTimingService.initDefaultTimingsIfAbsent(user.getId());
        taskService.initDefaultTasksIfAbsent(user.getId());

        return new AuthResponseDto("jwt_guest_" + user.getId(), "Welcome Guest Scholar!", user, progress);
    }

    @Transactional
    public UserProgress initUserProgressIfAbsent(User user) {
        return userProgressRepository.findByUserId(user.getId()).orElseGet(() -> {
            UserProgress newProgress = new UserProgress(user.getId());
            return userProgressRepository.save(newProgress);
        });
    }

    /**
     * Register a new user with Username and Password
     */
    @Transactional
    public AuthResponseDto registerWithUsernamePassword(UserAuthDto dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (dto.getPassword() == null || dto.getPassword().trim().length() < 3) {
            throw new IllegalArgumentException("Password must be at least 3 characters long.");
        }

        String username = dto.getUsername().trim();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken. Please choose another username.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(dto.getPassword().trim());
        String displayName = (dto.getName() != null && !dto.getName().trim().isBlank()) ? dto.getName().trim() : username;
        user.setName(displayName);
        user.setAvatar("https://api.dicebear.com/7.x/bottts/svg?seed=" + username);
        user.setAuthProvider("USERNAME_PASSWORD");
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLoginAt(LocalDateTime.now());
        user = userRepository.save(user);

        UserProgress progress = initUserProgressIfAbsent(user);
        studyTimingService.initDefaultTimingsIfAbsent(user.getId());
        taskService.initDefaultTasksIfAbsent(user.getId());

        String token = "jwt_user_" + user.getId() + "_" + System.currentTimeMillis();
        return new AuthResponseDto(token, "Account created successfully! Welcome, " + user.getName() + "!", user, progress);
    }

    /**
     * Login with Username and Password
     */
    @Transactional
    public AuthResponseDto loginWithUsernamePassword(UserAuthDto dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (dto.getPassword() == null || dto.getPassword().trim().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        String username = dto.getUsername().trim();
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User '" + username + "' not found. Please create an account first."));

        if (user.getPassword() != null && !user.getPassword().equals(dto.getPassword().trim())) {
            throw new IllegalArgumentException("Incorrect password. Please verify your password and try again.");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        UserProgress progress = initUserProgressIfAbsent(user);
        studyTimingService.initDefaultTimingsIfAbsent(user.getId());
        taskService.initDefaultTasksIfAbsent(user.getId());

        String token = "jwt_user_" + user.getId() + "_" + System.currentTimeMillis();
        return new AuthResponseDto(token, "Login successful! Welcome back, " + user.getName() + "!", user, progress);
    }

    /**
     * Edit / Update Username and Password anytime
     */
    @Transactional
    public User updateCredentials(Long userId, UserAuthDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (dto.getNewUsername() != null && !dto.getNewUsername().trim().isBlank()) {
            String newUsername = dto.getNewUsername().trim();
            if (!newUsername.equalsIgnoreCase(user.getUsername())) {
                if (userRepository.existsByUsernameIgnoreCase(newUsername)) {
                    throw new IllegalArgumentException("Username '" + newUsername + "' is already taken. Please pick another.");
                }
                user.setUsername(newUsername);
            }
        }

        if (dto.getNewPassword() != null && !dto.getNewPassword().trim().isBlank()) {
            if (dto.getNewPassword().trim().length() < 3) {
                throw new IllegalArgumentException("New password must be at least 3 characters.");
            }
            user.setPassword(dto.getNewPassword().trim());
        }

        if (dto.getName() != null && !dto.getName().trim().isBlank()) {
            user.setName(dto.getName().trim());
        }

        return userRepository.save(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    public SmsService getSmsService() {
        return this.smsService;
    }
}
