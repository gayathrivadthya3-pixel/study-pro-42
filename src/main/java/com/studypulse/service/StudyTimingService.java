package com.studypulse.service;

import com.studypulse.dto.StudyTimingDto;
import com.studypulse.model.StudyTiming;
import com.studypulse.repository.StudyTimingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudyTimingService {

    private final StudyTimingRepository studyTimingRepository;

    public StudyTimingService(StudyTimingRepository studyTimingRepository) {
        this.studyTimingRepository = studyTimingRepository;
    }

    /**
     * Initializes default study timings for the 4 core exam/learning tracks
     */
    @Transactional
    public void initDefaultTimingsIfAbsent(Long userId) {
        // Disabled: users start with an empty slate and only add their own timings
    }

    public List<StudyTimingDto> getTimings(Long userId, String category) {
        List<StudyTiming> timings;
        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            timings = studyTimingRepository.findByUserIdAndCategoryOrderByStartTimeAsc(userId, category.toUpperCase());
        } else {
            timings = studyTimingRepository.findByUserIdOrderByStartTimeAsc(userId);
        }
        return timings.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public StudyTimingDto createTiming(Long userId, StudyTimingDto dto) {
        StudyTiming timing = new StudyTiming();
        timing.setUserId(userId);
        timing.setCategory(dto.getCategory() != null ? dto.getCategory().toUpperCase() : "GENERAL");
        timing.setTitle(dto.getTitle());
        timing.setSubjectOrTopic(dto.getSubjectOrTopic());
        timing.setStartTime(dto.getStartTime());
        timing.setEndTime(dto.getEndTime());
        timing.setDaysOfWeek(dto.getDaysOfWeek() != null ? dto.getDaysOfWeek() : "MON,TUE,WED,THU,FRI,SAT,SUN");
        timing.setHasAlarm(dto.isHasAlarm());
        timing.setActive(true);
        return toDto(studyTimingRepository.save(timing));
    }

    @Transactional
    public StudyTimingDto updateTiming(Long userId, Long timingId, StudyTimingDto dto) {
        StudyTiming timing = studyTimingRepository.findById(timingId)
                .orElseThrow(() -> new IllegalArgumentException("Study Timing not found with id: " + timingId));

        if (!timing.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized timing edit.");
        }

        if (dto.getCategory() != null) timing.setCategory(dto.getCategory().toUpperCase());
        if (dto.getTitle() != null) timing.setTitle(dto.getTitle());
        if (dto.getSubjectOrTopic() != null) timing.setSubjectOrTopic(dto.getSubjectOrTopic());
        if (dto.getStartTime() != null) timing.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) timing.setEndTime(dto.getEndTime());
        if (dto.getDaysOfWeek() != null) timing.setDaysOfWeek(dto.getDaysOfWeek());
        timing.setHasAlarm(dto.isHasAlarm());
        timing.setActive(dto.isActive());

        return toDto(studyTimingRepository.save(timing));
    }

    @Transactional
    public void deleteTiming(Long userId, Long timingId) {
        StudyTiming timing = studyTimingRepository.findById(timingId)
                .orElseThrow(() -> new IllegalArgumentException("Study Timing not found with id: " + timingId));
        if (!timing.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized delete.");
        }
        studyTimingRepository.delete(timing);
    }

    @Transactional
    public void clearAllTimings(Long userId) {
        List<StudyTiming> timings = studyTimingRepository.findByUserIdOrderByStartTimeAsc(userId);
        studyTimingRepository.deleteAll(timings);
    }

    private StudyTimingDto toDto(StudyTiming timing) {
        StudyTimingDto dto = new StudyTimingDto();
        dto.setId(timing.getId());
        dto.setUserId(timing.getUserId());
        dto.setCategory(timing.getCategory());
        dto.setTitle(timing.getTitle());
        dto.setSubjectOrTopic(timing.getSubjectOrTopic());
        dto.setStartTime(timing.getStartTime());
        dto.setEndTime(timing.getEndTime());
        dto.setDaysOfWeek(timing.getDaysOfWeek());
        dto.setHasAlarm(timing.isHasAlarm());
        dto.setActive(timing.isActive());
        return dto;
    }
}
