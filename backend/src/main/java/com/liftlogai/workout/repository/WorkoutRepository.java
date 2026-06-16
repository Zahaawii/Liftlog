package com.liftlogai.workout.repository;

import com.liftlogai.workout.entity.Workout;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    Optional<Workout> findByIdAndUserId(Long id, Long userId);

    Page<Workout> findByUserId(Long userId, Pageable pageable);

    long countByUserIdAndWorkoutDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
