package com.studypulse.service;

import com.studypulse.dto.TaskDto;
import com.studypulse.model.Task;
import com.studypulse.repository.TaskRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final GamificationService gamificationService;

    public TaskService(TaskRepository taskRepository, @Lazy GamificationService gamificationService) {
        this.taskRepository = taskRepository;
        this.gamificationService = gamificationService;
    }

    /**
     * Initializes a rich set of realistic default daily tasks for a user
     */
    @Transactional
    public void initDefaultTasksIfAbsent(Long userId) {
        // Disabled: users start with an empty slate and only add their own tasks
    }

    /**
     * Load default tasks for a given category (or ALL) on a specific date
     */
    @Transactional
    public List<TaskDto> loadDefaultTasks(Long userId, String category, LocalDate targetDate) {
        if (targetDate == null) targetDate = LocalDate.now();
        String cat = (category != null && !category.isBlank()) ? category.toUpperCase() : "ALL";

        List<Task> newTasks = new ArrayList<>();

        // 1. SEMESTER EXAM DEFAULT TASKS
        if ("ALL".equals(cat) || "SEMESTER".equals(cat)) {
            Task t1 = new Task();
            t1.setUserId(userId);
            t1.setTitle("Semester: Read OS Concurrency & Deadlocks");
            t1.setDescription("Review Chapter 6 & 7 Peterson's algorithm, Banker's algorithm and solve 5 university questions.");
            t1.setCategory("SEMESTER");
            t1.setPriority("HIGH");
            t1.setScheduledDate(targetDate);
            t1.setScheduledTime("07:00");
            t1.setDurationMinutes(90);
            t1.setHasAlarm(true);
            t1.setCompleted(false);
            t1.setXpValue(50);
            newTasks.add(t1);

            Task t2 = new Task();
            t2.setUserId(userId);
            t2.setTitle("Semester: DBMS Normalization Practice");
            t2.setDescription("Solve 1NF, 2NF, 3NF, BCNF decomposition proofs from last 3 semester mid-term papers.");
            t2.setCategory("SEMESTER");
            t2.setPriority("MEDIUM");
            t2.setScheduledDate(targetDate);
            t2.setScheduledTime("18:30");
            t2.setDurationMinutes(60);
            t2.setHasAlarm(false);
            t2.setCompleted(false);
            t2.setXpValue(30);
            newTasks.add(t2);
        }

        // 2. GATE EXAM DEFAULT TASKS
        if ("ALL".equals(cat) || "GATE".equals(cat)) {
            Task t3 = new Task();
            t3.setUserId(userId);
            t3.setTitle("GATE: Linear Algebra & Eigenvalues PYQs");
            t3.setDescription("Solve 15 Previous Year Questions on Matrix Rank, Eigenvalues, and System of Equations (2015-2024).");
            t3.setCategory("GATE");
            t3.setPriority("HIGH");
            t3.setScheduledDate(targetDate);
            t3.setScheduledTime("09:30");
            t3.setDurationMinutes(120);
            t3.setHasAlarm(true);
            t3.setCompleted(false);
            t3.setXpValue(50);
            newTasks.add(t3);

            Task t4 = new Task();
            t4.setUserId(userId);
            t4.setTitle("GATE: Algorithms - Dynamic Programming & Graphs");
            t4.setDescription("Practice Dijkstra, Bellman-Ford, Floyd-Warshall and 0/1 Knapsack variations with strict 3 min/question timer.");
            t4.setCategory("GATE");
            t4.setPriority("HIGH");
            t4.setScheduledDate(targetDate);
            t4.setScheduledTime("14:30");
            t4.setDurationMinutes(120);
            t4.setHasAlarm(true);
            t4.setCompleted(false);
            t4.setXpValue(50);
            newTasks.add(t4);
        }

        // 3. OTHER ACTIVITIES DEFAULT TASKS
        if ("ALL".equals(cat) || "LANGUAGE".equals(cat)) {
            Task t5 = new Task();
            t5.setUserId(userId);
            t5.setTitle("Other Activities: Daily Reading & Habit Routine");
            t5.setDescription("30 minutes of high-focus reading, skill practice, or personal development notes.");
            t5.setCategory("LANGUAGE");
            t5.setPriority("MEDIUM");
            t5.setScheduledDate(targetDate);
            t5.setScheduledTime("08:00");
            t5.setDurationMinutes(40);
            t5.setHasAlarm(true);
            t5.setCompleted(false);
            t5.setXpValue(30);
            newTasks.add(t5);

            Task t6 = new Task();
            t6.setUserId(userId);
            t6.setTitle("Other Activities: Fitness, Meditation or Creative Project");
            t6.setDescription("Physical workout, mindfulness meditation, or creative hobby exploration block.");
            t6.setCategory("LANGUAGE");
            t6.setPriority("LOW");
            t6.setScheduledDate(targetDate);
            t6.setScheduledTime("21:30");
            t6.setDurationMinutes(30);
            t6.setHasAlarm(false);
            t6.setCompleted(false);
            t6.setXpValue(20);
            newTasks.add(t6);
        }

        // 4. INTERVIEW PREPARATION DEFAULT TASKS
        if ("ALL".equals(cat) || "INTERVIEW".equals(cat)) {
            Task t7 = new Task();
            t7.setUserId(userId);
            t7.setTitle("Interview: LeetCode Daily - Two Pointers & Sliding Window");
            t7.setDescription("Solve 1 Medium + 1 Hard problem. Write clean production-ready Java code with O(1) space optimization.");
            t7.setCategory("INTERVIEW");
            t7.setPriority("HIGH");
            t7.setScheduledDate(targetDate);
            t7.setScheduledTime("11:00");
            t7.setDurationMinutes(90);
            t7.setHasAlarm(true);
            t7.setCompleted(false);
            t7.setXpValue(50);
            newTasks.add(t7);

            Task t8 = new Task();
            t8.setUserId(userId);
            t8.setTitle("Interview: System Design - Rate Limiter & URL Shortener");
            t8.setDescription("Sketch architecture: Redis token bucket, load balancing, API gateway, and write STAR behavioral answer.");
            t8.setCategory("INTERVIEW");
            t8.setPriority("MEDIUM");
            t8.setScheduledDate(targetDate);
            t8.setScheduledTime("20:00");
            t8.setDurationMinutes(60);
            t8.setHasAlarm(true);
            t8.setCompleted(false);
            t8.setXpValue(40);
            newTasks.add(t8);
        }

        List<Task> saved = taskRepository.saveAll(newTasks);
        return saved.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<TaskDto> getTasks(Long userId, LocalDate date, String category) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        List<Task> tasks = taskRepository.findByUserIdAndScheduledDateOrderByScheduledTimeAsc(userId, queryDate);

        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            tasks = tasks.stream()
                    .filter(t -> category.equalsIgnoreCase(t.getCategory()))
                    .collect(Collectors.toList());
        }

        return tasks.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public TaskDto createTask(Long userId, TaskDto dto) {
        Task task = new Task();
        task.setUserId(userId);
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setCategory(dto.getCategory() != null ? dto.getCategory().toUpperCase() : "GENERAL");
        task.setPriority(dto.getPriority() != null ? dto.getPriority().toUpperCase() : "MEDIUM");
        task.setScheduledDate(dto.getScheduledDate() != null ? dto.getScheduledDate() : LocalDate.now());
        task.setScheduledTime(dto.getScheduledTime());
        task.setDurationMinutes(dto.getDurationMinutes() != null ? dto.getDurationMinutes() : 45);
        task.setHasAlarm(dto.isHasAlarm());
        task.setCompleted(false);

        // Assign XP based on priority
        int xp = 30;
        if ("HIGH".equalsIgnoreCase(task.getPriority())) xp = 50;
        if ("LOW".equalsIgnoreCase(task.getPriority())) xp = 20;
        task.setXpValue(dto.getXpValue() != null ? dto.getXpValue() : xp);

        return toDto(taskRepository.save(task));
    }

    /**
     * Edit / Update task details (title, description, time, priority, alarm, etc.)
     */
    @Transactional
    public TaskDto updateTask(Long userId, Long taskId, TaskDto dto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        if (!task.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized task update.");
        }

        if (dto.getTitle() != null) task.setTitle(dto.getTitle());
        if (dto.getDescription() != null) task.setDescription(dto.getDescription());
        if (dto.getCategory() != null) task.setCategory(dto.getCategory().toUpperCase());
        if (dto.getPriority() != null) task.setPriority(dto.getPriority().toUpperCase());
        if (dto.getScheduledDate() != null) task.setScheduledDate(dto.getScheduledDate());
        if (dto.getScheduledTime() != null) task.setScheduledTime(dto.getScheduledTime());
        if (dto.getDurationMinutes() != null) task.setDurationMinutes(dto.getDurationMinutes());
        task.setHasAlarm(dto.isHasAlarm());
        if (dto.getXpValue() != null) task.setXpValue(dto.getXpValue());

        return toDto(taskRepository.save(task));
    }

    /**
     * Toggle task completion and award XP
     */
    @Transactional
    public TaskDto toggleTaskCompletion(Long userId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        if (!task.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized task completion toggle.");
        }

        boolean willBeCompleted = !task.isCompleted();
        task.setCompleted(willBeCompleted);
        task.setCompletedAt(willBeCompleted ? LocalDateTime.now() : null);

        Task saved = taskRepository.save(task);

        // Award or deduct XP in gamification system
        gamificationService.handleTaskCompletion(userId, task.getXpValue(), willBeCompleted);

        return toDto(saved);
    }

    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));
        if (!task.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized task delete.");
        }
        taskRepository.delete(task);
    }

    @Transactional
    public void clearAllTasks(Long userId) {
        List<Task> tasks = taskRepository.findByUserIdOrderByScheduledDateAscScheduledTimeAsc(userId);
        taskRepository.deleteAll(tasks);
    }

    public TaskDto toDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setUserId(task.getUserId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setCategory(task.getCategory());
        dto.setPriority(task.getPriority());
        dto.setScheduledDate(task.getScheduledDate());
        dto.setScheduledTime(task.getScheduledTime());
        dto.setDurationMinutes(task.getDurationMinutes());
        dto.setHasAlarm(task.isHasAlarm());
        dto.setCompleted(task.isCompleted());
        dto.setXpValue(task.getXpValue());
        return dto;
    }
}
