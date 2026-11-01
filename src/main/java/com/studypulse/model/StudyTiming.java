package com.studypulse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_timings")
public class StudyTiming {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category", nullable = false)
    private String category; // SEMESTER, GATE, LANGUAGE, INTERVIEW

    @Column(name = "title", nullable = false)
    private String title; // e.g. "Data Structures & Algorithms Drill", "GATE Engineering Math"

    @Column(name = "subject_or_topic")
    private String subjectOrTopic; // e.g. "Operating Systems", "German B1 Vocab"

    @Column(name = "start_time", nullable = false)
    private String startTime; // "06:00"

    @Column(name = "end_time", nullable = false)
    private String endTime; // "08:30"

    @Column(name = "days_of_week")
    private String daysOfWeek; // "MON,TUE,WED,THU,FRI,SAT,SUN"

    @Column(name = "has_alarm", nullable = false)
    private boolean hasAlarm;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public StudyTiming() {}

    public StudyTiming(Long userId, String category, String title, String subjectOrTopic,
                       String startTime, String endTime, String daysOfWeek, boolean hasAlarm) {
        this.userId = userId;
        this.category = category;
        this.title = title;
        this.subjectOrTopic = subjectOrTopic;
        this.startTime = startTime;
        this.endTime = endTime;
        this.daysOfWeek = daysOfWeek;
        this.hasAlarm = hasAlarm;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (daysOfWeek == null) daysOfWeek = "MON,TUE,WED,THU,FRI,SAT,SUN";
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubjectOrTopic() {
        return subjectOrTopic;
    }

    public void setSubjectOrTopic(String subjectOrTopic) {
        this.subjectOrTopic = subjectOrTopic;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public boolean isHasAlarm() {
        return hasAlarm;
    }

    public void setHasAlarm(boolean hasAlarm) {
        this.hasAlarm = hasAlarm;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
