package com.studypulse.service;

import com.studypulse.dto.GamifiedProgressDto;
import com.studypulse.model.UserProgress;
import com.studypulse.repository.TaskRepository;
import com.studypulse.repository.UserProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class GamificationService {

    private final UserProgressRepository userProgressRepository;
    private final TaskRepository taskRepository;

    // Level XP thresholds
    private static final int[] LEVEL_THRESHOLDS = {
            0,     // Level 1 starts at 0
            150,   // Level 2
            400,   // Level 3
            800,   // Level 4
            1400,  // Level 5
            2200,  // Level 6
            3200,  // Level 7
            4500,  // Level 8
            6000,  // Level 9
            8000   // Level 10
    };

    private static final String[] LEVEL_TITLES = {
            "Novice Scholar",         // L1
            "Disciplined Apprentice", // L2
            "Problem Solver",         // L3
            "GATE Strategist",        // L4
            "Polyglot Explorer",      // L5
            "Interview Ace",          // L6
            "Academic Titan",         // L7
            "Master of Systems",      // L8
            "Grandmaster Scholar",    // L9
            "Legendary Polymath"      // L10
    };

    public GamificationService(UserProgressRepository userProgressRepository, TaskRepository taskRepository) {
        this.userProgressRepository = userProgressRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public void handleTaskCompletion(Long userId, int xpValue, boolean isCompleted) {
        UserProgress progress = userProgressRepository.findByUserId(userId).orElseGet(() -> {
            UserProgress up = new UserProgress(userId);
            return userProgressRepository.save(up);
        });

        int currentXp = progress.getTotalXp();
        int completedCount = progress.getTotalTasksCompleted();

        if (isCompleted) {
            currentXp += xpValue;
            completedCount += 1;
            updateStreak(progress);
        } else {
            currentXp = Math.max(0, currentXp - xpValue);
            completedCount = Math.max(0, completedCount - 1);
        }

        progress.setTotalXp(currentXp);
        progress.setTotalTasksCompleted(completedCount);

        // Recalculate Level & Title
        int level = calculateLevel(currentXp);
        progress.setCurrentLevel(level);
        progress.setLevelTitle(LEVEL_TITLES[Math.min(level - 1, LEVEL_TITLES.length - 1)]);

        // Check & unlock badges
        Set<String> badges = parseBadges(progress.getBadgesJson());
        if (completedCount >= 1) badges.add("FIRST_STEP");
        if (progress.getCurrentStreak() >= 3) badges.add("STREAK_3");
        if (progress.getCurrentStreak() >= 7) badges.add("STREAK_7");
        if (currentXp >= 1000) badges.add("CENTURION");
        if (level >= 5) badges.add("LEVEL_5_MASTER");

        progress.setBadgesJson(serializeBadges(badges));
        userProgressRepository.save(progress);
    }

    private void updateStreak(UserProgress progress) {
        LocalDate today = LocalDate.now();
        LocalDate lastActive = progress.getLastActiveDate();

        if (lastActive == null) {
            progress.setCurrentStreak(1);
            progress.setLastActiveDate(today);
        } else if (lastActive.equals(today.minusDays(1))) {
            // Consecutive day!
            int newStreak = progress.getCurrentStreak() + 1;
            progress.setCurrentStreak(newStreak);
            if (newStreak > progress.getBestStreak()) {
                progress.setBestStreak(newStreak);
            }
            progress.setLastActiveDate(today);
        } else if (!lastActive.equals(today)) {
            // Missed a day or first time in a while
            progress.setCurrentStreak(1);
            progress.setLastActiveDate(today);
        }
    }

    public GamifiedProgressDto getProgress(Long userId) {
        UserProgress progress = userProgressRepository.findByUserId(userId).orElseGet(() -> {
            UserProgress up = new UserProgress(userId);
            return userProgressRepository.save(up);
        });

        LocalDate today = LocalDate.now();
        long todayTotal = taskRepository.countByUserIdAndScheduledDate(userId, today);
        long todayCompleted = taskRepository.countByUserIdAndScheduledDateAndCompletedTrue(userId, today);

        int level = progress.getCurrentLevel();
        int baseLevelXp = LEVEL_THRESHOLDS[Math.min(level - 1, LEVEL_THRESHOLDS.length - 1)];
        int nextLevelXp = (level < LEVEL_THRESHOLDS.length) ? LEVEL_THRESHOLDS[level] : baseLevelXp + 2000;

        int totalXp = progress.getTotalXp();
        int xpInCurrentLevel = Math.max(0, totalXp - baseLevelXp);
        int levelSpan = Math.max(1, nextLevelXp - baseLevelXp);
        int progressPercent = Math.min(100, (int) Math.round(((double) xpInCurrentLevel / levelSpan) * 100));

        int todayPercent = (todayTotal > 0) ? (int) Math.round(((double) todayCompleted / todayTotal) * 100) : 0;

        GamifiedProgressDto dto = new GamifiedProgressDto();
        dto.setTotalXp(totalXp);
        dto.setCurrentLevel(level);
        dto.setLevelTitle(progress.getLevelTitle());
        dto.setNextLevelXp(nextLevelXp);
        dto.setLevelBaseXp(baseLevelXp);
        dto.setProgressPercent(progressPercent);
        dto.setCurrentStreak(progress.getCurrentStreak());
        dto.setBestStreak(progress.getBestStreak());
        dto.setTotalTasksCompleted(progress.getTotalTasksCompleted());
        dto.setTotalStudyMinutes(progress.getTotalStudyMinutes());
        dto.setBadges(new ArrayList<>(parseBadges(progress.getBadgesJson())));
        dto.setTodayCompletedCount((int) todayCompleted);
        dto.setTodayTotalCount((int) todayTotal);
        dto.setTodayProgressPercent(todayPercent);

        return dto;
    }

    private int calculateLevel(int xp) {
        for (int i = LEVEL_THRESHOLDS.length - 1; i >= 0; i--) {
            if (xp >= LEVEL_THRESHOLDS[i]) {
                return i + 1;
            }
        }
        return 1;
    }

    private Set<String> parseBadges(String json) {
        Set<String> set = new LinkedHashSet<>();
        if (json == null || json.isBlank()) return set;
        String clean = json.replace("[", "").replace("]", "").replace("\"", "");
        for (String b : clean.split(",")) {
            String trimmed = b.trim();
            if (!trimmed.isEmpty()) set.add(trimmed);
        }
        return set;
    }

    private String serializeBadges(Set<String> badges) {
        StringBuilder sb = new StringBuilder("[");
        int i = 0;
        for (String b : badges) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(b).append("\"");
            i++;
        }
        sb.append("]");
        return sb.toString();
    }
}
