package com.studypulse.repository;

import com.studypulse.model.StudyTiming;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyTimingRepository extends JpaRepository<StudyTiming, Long> {
    List<StudyTiming> findByUserIdOrderByStartTimeAsc(Long userId);
    List<StudyTiming> findByUserIdAndCategoryOrderByStartTimeAsc(Long userId, String category);
    long countByUserId(Long userId);
}
