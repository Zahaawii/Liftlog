package com.liftlogai.exercise.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "exercises")
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 160)
    private String name;

    @Column(length = 80)
    private String category;

    @Column(name = "primary_muscle_group", length = 80)
    private String primaryMuscleGroup;

    @Column(name = "measurement_type", nullable = false, length = 40)
    private String measurementType;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(nullable = false, length = 40)
    private String source;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Exercise() {
    }

    public Exercise(String name, String category, String primaryMuscleGroup, String measurementType, String source) {
        this.name = name;
        this.category = category;
        this.primaryMuscleGroup = primaryMuscleGroup;
        this.measurementType = measurementType;
        this.source = source;
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

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getPrimaryMuscleGroup() {
        return primaryMuscleGroup;
    }

    public String getMeasurementType() {
        return measurementType;
    }

    public boolean isActive() {
        return active;
    }

    public String getSource() {
        return source;
    }
}
