package com.liftlogai.goal.entity;

import com.liftlogai.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goals")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "goal_type", nullable = false, length = 40)
    private String goalType;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(name = "target_metric", nullable = false, length = 80)
    private String targetMetric;

    @Column(name = "target_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal targetValue;

    @Column(name = "current_baseline", precision = 12, scale = 2)
    private BigDecimal currentBaseline;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private GoalStatus status = GoalStatus.ACTIVE;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoalCheckIn> checkIns = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Goal() {
    }

    public Goal(
            User user,
            String goalType,
            String title,
            String targetMetric,
            BigDecimal targetValue,
            BigDecimal currentBaseline,
            LocalDate startDate,
            LocalDate targetDate,
            GoalStatus status
    ) {
        this.user = user;
        this.goalType = goalType;
        this.title = title;
        this.targetMetric = targetMetric;
        this.targetValue = targetValue;
        this.currentBaseline = currentBaseline;
        this.startDate = startDate;
        this.targetDate = targetDate;
        this.status = status;
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

    public User getUser() {
        return user;
    }

    public String getGoalType() {
        return goalType;
    }

    public String getTitle() {
        return title;
    }

    public String getTargetMetric() {
        return targetMetric;
    }

    public BigDecimal getTargetValue() {
        return targetValue;
    }

    public BigDecimal getCurrentBaseline() {
        return currentBaseline;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(
            String goalType,
            String title,
            String targetMetric,
            BigDecimal targetValue,
            BigDecimal currentBaseline,
            LocalDate startDate,
            LocalDate targetDate,
            GoalStatus status
    ) {
        this.goalType = goalType;
        this.title = title;
        this.targetMetric = targetMetric;
        this.targetValue = targetValue;
        this.currentBaseline = currentBaseline;
        this.startDate = startDate;
        this.targetDate = targetDate;
        this.status = status;
    }
}
