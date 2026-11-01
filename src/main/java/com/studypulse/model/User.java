package com.studypulse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "mobile_number", unique = true, nullable = true)
    private String mobileNumber;

    @Column(name = "email", unique = true, nullable = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "auth_provider", nullable = false)
    private String authProvider; // MOBILE_OTP, GOOGLE, GUEST

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Study Targets
    @Column(name = "target_semester_exam")
    private String targetSemesterExam;

    @Column(name = "target_gate_year")
    private String targetGateYear;

    @Column(name = "target_language")
    private String targetLanguage;

    @Column(name = "target_interview_role")
    private String targetInterviewRole;

    public User() {
    }

    public User(Long id, String mobileNumber, String email, String name, String avatar, String authProvider) {
        this.id = id;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.name = name;
        this.avatar = avatar;
        this.authProvider = authProvider;
        this.createdAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (lastLoginAt == null) lastLoginAt = LocalDateTime.now();
        if (targetSemesterExam == null) targetSemesterExam = "Upcoming Semester Final Exams";
        if (targetGateYear == null) targetGateYear = "GATE 2027";
        if (targetLanguage == null) targetLanguage = "German / Japanese / English";
        if (targetInterviewRole == null) targetInterviewRole = "Full Stack & SDE 1";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getTargetSemesterExam() {
        return targetSemesterExam;
    }

    public void setTargetSemesterExam(String targetSemesterExam) {
        this.targetSemesterExam = targetSemesterExam;
    }

    public String getTargetGateYear() {
        return targetGateYear;
    }

    public void setTargetGateYear(String targetGateYear) {
        this.targetGateYear = targetGateYear;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public String getTargetInterviewRole() {
        return targetInterviewRole;
    }

    public void setTargetInterviewRole(String targetInterviewRole) {
        this.targetInterviewRole = targetInterviewRole;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
