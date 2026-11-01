package com.studypulse.dto;

import java.util.List;

public class GamifiedProgressDto {
    private int totalXp;
    private int currentLevel;
    private String levelTitle;
    private int nextLevelXp;
    private int levelBaseXp;
    private int progressPercent;
    private int currentStreak;
    private int bestStreak;
    private int totalTasksCompleted;
    private int totalStudyMinutes;
    private List<String> badges;
    private int todayCompletedCount;
    private int todayTotalCount;
    private int todayProgressPercent;

    public GamifiedProgressDto() {}

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

    public int getNextLevelXp() {
        return nextLevelXp;
    }

    public void setNextLevelXp(int nextLevelXp) {
        this.nextLevelXp = nextLevelXp;
    }

    public int getLevelBaseXp() {
        return levelBaseXp;
    }

    public void setLevelBaseXp(int levelBaseXp) {
        this.levelBaseXp = levelBaseXp;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
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

    public List<String> getBadges() {
        return badges;
    }

    public void setBadges(List<String> badges) {
        this.badges = badges;
    }

    public int getTodayCompletedCount() {
        return todayCompletedCount;
    }

    public void setTodayCompletedCount(int todayCompletedCount) {
        this.todayCompletedCount = todayCompletedCount;
    }

    public int getTodayTotalCount() {
        return todayTotalCount;
    }

    public void setTodayTotalCount(int todayTotalCount) {
        this.todayTotalCount = todayTotalCount;
    }

    public int getTodayProgressPercent() {
        return todayProgressPercent;
    }

    public void setTodayProgressPercent(int todayProgressPercent) {
        this.todayProgressPercent = todayProgressPercent;
    }
}
