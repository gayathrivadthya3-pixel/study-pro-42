package com.studypulse.repository;

import com.studypulse.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserIdOrderByScheduledDateAscScheduledTimeAsc(Long userId);
    List<Task> findByUserIdAndScheduledDateOrderByScheduledTimeAsc(Long userId, LocalDate date);
    List<Task> findByUserIdAndScheduledDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<Task> findByUserIdAndCategory(Long userId, String category);
    long countByUserId(Long userId);
    long countByUserIdAndCompletedTrue(Long userId);
    long countByUserIdAndScheduledDate(Long userId, LocalDate date);
    long countByUserIdAndScheduledDateAndCompletedTrue(Long userId, LocalDate date);
}
