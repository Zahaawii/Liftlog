package com.liftlogai.nutrition.entity;

import com.liftlogai.user.entity.User;
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
import java.time.LocalDate;

@Entity
@Table(name = "nutrition_logs")
public class NutritionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "meal_type", nullable = false, length = 40)
    private String mealType;

    @Column(name = "food_name", nullable = false, length = 160)
    private String foodName;

    @Column(name = "serving_quantity", precision = 8, scale = 2)
    private BigDecimal servingQuantity;

    private Integer calories;

    @Column(precision = 8, scale = 2)
    private BigDecimal protein;

    @Column(precision = 8, scale = 2)
    private BigDecimal carbohydrates;

    @Column(precision = 8, scale = 2)
    private BigDecimal fat;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected NutritionLog() {
    }

    public NutritionLog(
            User user,
            LocalDate logDate,
            String mealType,
            String foodName,
            BigDecimal servingQuantity,
            Integer calories,
            BigDecimal protein,
            BigDecimal carbohydrates,
            BigDecimal fat,
            String notes
    ) {
        this.user = user;
        this.logDate = logDate;
        this.mealType = mealType;
        this.foodName = foodName;
        this.servingQuantity = servingQuantity;
        this.calories = calories;
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
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

    public LocalDate getLogDate() {
        return logDate;
    }

    public String getMealType() {
        return mealType;
    }

    public String getFoodName() {
        return foodName;
    }

    public BigDecimal getServingQuantity() {
        return servingQuantity;
    }

    public Integer getCalories() {
        return calories;
    }

    public BigDecimal getProtein() {
        return protein;
    }

    public BigDecimal getCarbohydrates() {
        return carbohydrates;
    }

    public BigDecimal getFat() {
        return fat;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(
            LocalDate logDate,
            String mealType,
            String foodName,
            BigDecimal servingQuantity,
            Integer calories,
            BigDecimal protein,
            BigDecimal carbohydrates,
            BigDecimal fat,
            String notes
    ) {
        this.logDate = logDate;
        this.mealType = mealType;
        this.foodName = foodName;
        this.servingQuantity = servingQuantity;
        this.calories = calories;
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
        this.notes = notes;
    }
}
