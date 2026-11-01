package com.studypulse.service;

import com.studypulse.dto.AppreciationDto;
import com.studypulse.model.Task;
import com.studypulse.model.User;
import com.studypulse.model.UserProgress;
import com.studypulse.repository.TaskRepository;
import com.studypulse.repository.UserProgressRepository;
import com.studypulse.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppreciationService {

    private final TaskRepository taskRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;

    private static final String[] DAILY_QUOTES = {
            "\"Excellence is not an act, but a habit. What you achieved today defines your future.\" - Aristotle",
            "\"To succeed in your mission, you must have single-minded devotion to your goal.\" - Dr. A.P.J. Abdul Kalam",
            "\"Small daily improvements over time lead to stunning results.\" - Robin Sharma",
            "\"Discipline is the bridge between goals and accomplishment.\" - Jim Rohn",
            "\"Arise, awake, and stop not until the goal is reached.\" - Swami Vivekananda"
    };

    private static final String[] WEEKLY_QUOTES = {
            "\"Champions do not become champions when they win the event, but in the hours, weeks, and months spent preparing for it.\" - Michael Phelps",
            "\"Grit is living life like it's a marathon, not a sprint.\" - Dr. Angela Duckworth",
            "\"Success isn't always about greatness. It's about consistency. Consistent hard work leads to success.\" - Dwayne Johnson"
    };

    public AppreciationService(TaskRepository taskRepository,
                               UserProgressRepository userProgressRepository,
                               UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userProgressRepository = userProgressRepository;
        this.userRepository = userRepository;
    }

    /**
     * Evaluates daily performance and returns celebratory appreciation if qualified
     */
    @Transactional
    public AppreciationDto evaluateDailyPerformance(Long userId) {
        LocalDate today = LocalDate.now();
        List<Task> todayTasks = taskRepository.findByUserIdAndScheduledDateOrderByScheduledTimeAsc(userId, today);
        User user = userRepository.findById(userId).orElse(null);
        String userName = (user != null) ? user.getName() : "Scholar";

        AppreciationDto dto = new AppreciationDto();
        dto.setType("DAILY");

        if (todayTasks.isEmpty()) {
            dto.setQualified(false);
            dto.setHeadline("Start Your Journey Today");
            dto.setMessage("No tasks scheduled for today yet. Add tasks or load your default study plan to unlock daily rewards!");
            return dto;
        }

        long completedCount = todayTasks.stream().filter(Task::isCompleted).count();
        int totalCount = todayTasks.size();
        int percent = (int) Math.round(((double) completedCount / totalCount) * 100);

        dto.setTasksCompleted((int) completedCount);
        dto.setTotalTasks(totalCount);
        dto.setCompletionPercentage(percent);

        // Qualify if >= 70% completed or all tasks completed
        if (percent >= 70) {
            dto.setQualified(true);
            dto.setHeadline("🌟 Outstanding Achievement Today, " + userName + "!");
            dto.setMessage("You conquered " + completedCount + " of " + totalCount + " planned study sessions today (" + percent + "% completion)! " +
                    "Your dedication toward your Semester exams, GATE preparation, Other Activities, and Interview targets is world-class. Keep this momentum blazing!");
            dto.setMotivationalQuote(DAILY_QUOTES[(int) (System.currentTimeMillis() % DAILY_QUOTES.length)]);
            dto.setBonusXp(50);
            dto.setBadgeAwarded("DAILY_STAR");
            dto.setCelebrationTheme("GOLDEN_CHAMPION");

            // Award daily bonus XP if not awarded yet
            userProgressRepository.findByUserId(userId).ifPresent(progress -> {
                dto.setStreakCount(progress.getCurrentStreak());
            });
        } else {
            dto.setQualified(false);
            dto.setHeadline("Keep Pushing Forward!");
            dto.setMessage("You've completed " + completedCount + " of " + totalCount + " tasks today (" + percent + "%). Finish " +
                    Math.max(1, (int) Math.ceil(totalCount * 0.70) - completedCount) + " more task(s) to unlock your Daily High-Performer Appreciation Trophy!");
            dto.setMotivationalQuote(DAILY_QUOTES[0]);
        }

        return dto;
    }

    /**
     * Evaluates weekly performance (past 7 days)
     */
    @Transactional
    public AppreciationDto evaluateWeeklyPerformance(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);
        List<Task> weekTasks = taskRepository.findByUserIdAndScheduledDateBetween(userId, weekAgo, today);
        User user = userRepository.findById(userId).orElse(null);
        String userName = (user != null) ? user.getName() : "Scholar";

        AppreciationDto dto = new AppreciationDto();
        dto.setType("WEEKLY");

        if (weekTasks.isEmpty()) {
            dto.setQualified(false);
            dto.setHeadline("Weekly Progress Tracker");
            dto.setMessage("Keep using Study Pro 42 throughout the week to receive comprehensive weekly praise and rewards.");
            return dto;
        }

        long completed = weekTasks.stream().filter(Task::isCompleted).count();
        int total = weekTasks.size();
        int percent = (int) Math.round(((double) completed / total) * 100);

        dto.setTasksCompleted((int) completed);
        dto.setTotalTasks(total);
        dto.setCompletionPercentage(percent);

        if (percent >= 65 && completed >= 5) {
            dto.setQualified(true);
            dto.setHeadline("👑 Weekly Grandmaster Consistency, " + userName + "!");
            dto.setMessage("Phenomenal weekly stamina! You successfully conquered " + completed + " tasks over the past 7 days with a " + percent + "% mastery rate. " +
                    "Your deliberate practice across GATE, Semester, Coding & Other Activities places you among the top 1% disciplined learners!");
            dto.setMotivationalQuote(WEEKLY_QUOTES[(int) (System.currentTimeMillis() % WEEKLY_QUOTES.length)]);
            dto.setBonusXp(150);
            dto.setBadgeAwarded("WEEKLY_CHAMPION");
            dto.setCelebrationTheme("STREAK_MASTER");
        } else {
            dto.setQualified(false);
            dto.setHeadline("Weekly Progress Summary");
            dto.setMessage("You have completed " + completed + " out of " + total + " tasks this week (" + percent + "%). Maintain your daily study rhythm to claim this week's Crown!");
            dto.setMotivationalQuote(WEEKLY_QUOTES[0]);
        }

        return dto;
    }
}
