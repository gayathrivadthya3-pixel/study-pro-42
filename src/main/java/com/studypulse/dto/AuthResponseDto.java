package com.studypulse.dto;

import com.studypulse.model.User;
import com.studypulse.model.UserProgress;

public class AuthResponseDto {
    private String token;
    private String message;
    private User user;
    private UserProgress progress;

    public AuthResponseDto() {}

    public AuthResponseDto(String token, String message, User user, UserProgress progress) {
        this.token = token;
        this.message = message;
        this.user = user;
        this.progress = progress;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserProgress getProgress() {
        return progress;
    }

    public void setProgress(UserProgress progress) {
        this.progress = progress;
    }
}
