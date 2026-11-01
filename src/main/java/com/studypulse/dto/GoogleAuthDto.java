package com.studypulse.dto;

public class GoogleAuthDto {
    private String email;
    private String name;
    private String avatar;
    private String googleId;

    public GoogleAuthDto() {}

    public GoogleAuthDto(String email, String name, String avatar, String googleId) {
        this.email = email;
        this.name = name;
        this.avatar = avatar;
        this.googleId = googleId;
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

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }
}
