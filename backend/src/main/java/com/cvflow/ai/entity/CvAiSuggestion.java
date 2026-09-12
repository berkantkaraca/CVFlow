package com.cvflow.ai.entity;

import com.cvflow.ai.model.AiOperation;
import com.cvflow.ai.model.AiSuggestionPayload;
import com.cvflow.ai.model.AiSuggestionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "cv_ai_suggestions")
public class CvAiSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "language_version_id", nullable = false)
    private UUID languageVersionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AiOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AiSuggestionStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private AiSuggestionPayload payload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CvAiSuggestion() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getLanguageVersionId() {
        return languageVersionId;
    }

    public void setLanguageVersionId(UUID languageVersionId) {
        this.languageVersionId = languageVersionId;
    }

    public AiOperation getOperation() {
        return operation;
    }

    public void setOperation(AiOperation operation) {
        this.operation = operation;
    }

    public AiSuggestionStatus getStatus() {
        return status;
    }

    public void setStatus(AiSuggestionStatus status) {
        this.status = status;
    }

    public AiSuggestionPayload getPayload() {
        return payload;
    }

    public void setPayload(AiSuggestionPayload payload) {
        this.payload = payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
