package com.liftlogai.exercise.repository;

import com.liftlogai.exercise.entity.Exercise;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    List<Exercise> findByActiveTrueOrderByNameAsc();

    Optional<Exercise> findByIdAndActiveTrue(Long id);

    boolean existsByName(String name);
}
