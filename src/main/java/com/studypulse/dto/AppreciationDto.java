package com.studypulse.dto;

public class AppreciationDto {
    private boolean qualified;
    private String type; // DAILY or WEEKLY
    private String headline;
    private String message;
    private String motivationalQuote;
    private int tasksCompleted;
    private int totalTasks;
    private int completionPercentage;
    private int streakCount;
    private int bonusXp;
    private String badgeAwarded;
    private String celebrationTheme; // "GOLDEN_CHAMPION", "STREAK_MASTER", "GATE_CONQUEROR", "POLYGLOT"

    public AppreciationDto() {}

    public boolean isQualified() {
        return qualified;
    }

    public void setQualified(boolean qualified) {
        this.qualified = qualified;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMotivationalQuote() {
        return motivationalQuote;
    }

    public void setMotivationalQuote(String motivationalQuote) {
        this.motivationalQuote = motivationalQuote;
    }

    public int getTasksCompleted() {
        return tasksCompleted;
    }

    public void setTasksCompleted(int tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public int getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(int completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public int getStreakCount() {
        return streakCount;
    }

    public void setStreakCount(int streakCount) {
        this.streakCount = streakCount;
    }

    public int getBonusXp() {
        return bonusXp;
    }

    public void setBonusXp(int bonusXp) {
        this.bonusXp = bonusXp;
    }

    public String getBadgeAwarded() {
        return badgeAwarded;
    }

    public void setBadgeAwarded(String badgeAwarded) {
        this.badgeAwarded = badgeAwarded;
    }

    public String getCelebrationTheme() {
        return celebrationTheme;
    }

    public void setCelebrationTheme(String celebrationTheme) {
        this.celebrationTheme = celebrationTheme;
    }
}
