package com.liftlogai.workout.entity;

import com.liftlogai.exercise.entity.Exercise;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_exercises")
public class WorkoutExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("setNumber ASC")
    private List<WorkoutSet> sets = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected WorkoutExercise() {
    }

    public WorkoutExercise(Exercise exercise, int displayOrder, String notes) {
        this.exercise = exercise;
        this.displayOrder = displayOrder;
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

    public Workout getWorkout() {
        return workout;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public String getNotes() {
        return notes;
    }

    public List<WorkoutSet> getSets() {
        return sets;
    }

    void assignWorkout(Workout workout) {
        this.workout = workout;
        sets.forEach(set -> set.assignWorkoutExercise(this));
    }

    public void replaceSets(List<WorkoutSet> replacementSets) {
        sets.clear();
        replacementSets.forEach(this::addSet);
    }

    private void addSet(WorkoutSet workoutSet) {
        workoutSet.assignWorkoutExercise(this);
        sets.add(workoutSet);
    }
}
