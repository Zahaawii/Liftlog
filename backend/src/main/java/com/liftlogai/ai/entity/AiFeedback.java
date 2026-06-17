package com.liftlogai.ai.entity;

import com.liftlogai.user.entity.User;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ai_feedback")
public class AiFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "request_type", nullable = false, length = 80)
    private String requestType;

    @Column(nullable = false, length = 80)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AiFeedbackStatus status;

    @Column(name = "prompt_summary", nullable = false, length = 1000)
    private String promptSummary;

    @Column(nullable = false, length = 1000)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "error_code", length = 120)
    private String errorCode;

    protected AiFeedback() {
    }

    public AiFeedback(
            User user,
            String requestType,
            String provider,
            AiFeedbackStatus status,
            String promptSummary,
            String summary,
            String recommendations,
            String feedback
    ) {
        this.user = user;
        this.requestType = requestType;
        this.provider = provider;
        this.status = status;
        this.promptSummary = promptSummary;
        this.summary = summary;
        this.recommendations = recommendations;
        this.feedback = feedback;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        completedAt = now;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getProvider() {
        return provider;
    }

    public AiFeedbackStatus getStatus() {
        return status;
    }

    public String getPromptSummary() {
        return promptSummary;
    }

    public String getSummary() {
        return summary;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public String getFeedback() {
        return feedback;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
