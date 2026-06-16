package com.liftlogai.workout.repository;

import com.liftlogai.workout.entity.WorkoutExercise;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {

    long countByExerciseId(Long exerciseId);

    @Query("""
            select count(distinct w.id),
                   count(ws.id),
                   max(ws.reps),
                   max(ws.weight),
                   coalesce(sum(ws.weight * ws.reps), 0)
            from WorkoutExercise we
            join we.workout w
            left join we.sets ws on ws.completed = true
            where w.user.id = :userId
              and we.exercise.id = :exerciseId
            """)
    List<Object[]> progressionAggregate(@Param("userId") Long userId, @Param("exerciseId") Long exerciseId);
}
