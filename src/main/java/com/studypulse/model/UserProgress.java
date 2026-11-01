package com.studypulse.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_progress")
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "total_xp", nullable = false)
    private int totalXp;

    @Column(name = "current_level", nullable = false)
    private int currentLevel;

    @Column(name = "level_title", nullable = false)
    private String levelTitle;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak;

    @Column(name = "best_streak", nullable = false)
    private int bestStreak;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @Column(name = "total_tasks_completed", nullable = false)
    private int totalTasksCompleted;

    @Column(name = "total_study_minutes", nullable = false)
    private int totalStudyMinutes;

    @Column(name = "badges_json", length = 2000)
    private String badgesJson; // e.g. JSON array of badge keys

    public UserProgress() {}

    public UserProgress(Long userId) {
        this.userId = userId;
        this.totalXp = 50; // Starter welcome XP
        this.currentLevel = 1;
        this.levelTitle = "Novice Scholar";
        this.currentStreak = 1;
        this.bestStreak = 1;
        this.lastActiveDate = LocalDate.now();
        this.totalTasksCompleted = 0;
        this.totalStudyMinutes = 0;
        this.badgesJson = "[\"EARLY_BIRD\",\"FIRST_LOGIN\"]";
    }

    @PrePersist
    protected void onCreate() {
        if (levelTitle == null) levelTitle = "Novice Scholar";
        if (badgesJson == null) badgesJson = "[\"FIRST_LOGIN\"]";
        if (lastActiveDate == null) lastActiveDate = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(int totalXp) {
        this.totalXp = totalXp;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public String getLevelTitle() {
        return levelTitle;
    }

    public void setLevelTitle(String levelTitle) {
        this.levelTitle = levelTitle;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public void setBestStreak(int bestStreak) {
        this.bestStreak = bestStreak;
    }

    public LocalDate getLastActiveDate() {
        return lastActiveDate;
    }

    public void setLastActiveDate(LocalDate lastActiveDate) {
        this.lastActiveDate = lastActiveDate;
    }

    public int getTotalTasksCompleted() {
        return totalTasksCompleted;
    }

    public void setTotalTasksCompleted(int totalTasksCompleted) {
        this.totalTasksCompleted = totalTasksCompleted;
    }

    public int getTotalStudyMinutes() {
        return totalStudyMinutes;
    }

    public void setTotalStudyMinutes(int totalStudyMinutes) {
        this.totalStudyMinutes = totalStudyMinutes;
    }

    public String getBadgesJson() {
        return badgesJson;
    }

    public void setBadgesJson(String badgesJson) {
        this.badgesJson = badgesJson;
    }
}
