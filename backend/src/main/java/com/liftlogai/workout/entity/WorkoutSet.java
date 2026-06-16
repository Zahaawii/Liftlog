package com.liftlogai.workout.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "workout_sets")
public class WorkoutSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_exercise_id", nullable = false)
    private WorkoutExercise workoutExercise;

    @Column(name = "set_number", nullable = false)
    private int setNumber;

    private Integer reps;

    @Column(precision = 8, scale = 2)
    private BigDecimal weight;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(precision = 10, scale = 2)
    private BigDecimal distance;

    @Column(nullable = false)
    private boolean completed;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected WorkoutSet() {
    }

    public WorkoutSet(
            int setNumber,
            Integer reps,
            BigDecimal weight,
            Integer durationSeconds,
            BigDecimal distance,
            boolean completed,
            String notes
    ) {
        this.setNumber = setNumber;
        this.reps = reps;
        this.weight = weight;
        this.durationSeconds = durationSeconds;
        this.distance = distance;
        this.completed = completed;
        this.notes = notes;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public int getSetNumber() {
        return setNumber;
    }

    public Integer getReps() {
        return reps;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public BigDecimal getDistance() {
        return distance;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getNotes() {
        return notes;
    }

    void assignWorkoutExercise(WorkoutExercise workoutExercise) {
        this.workoutExercise = workoutExercise;
    }
}
