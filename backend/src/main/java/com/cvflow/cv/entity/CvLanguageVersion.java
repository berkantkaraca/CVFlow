package com.cvflow.cv.entity;

import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name = "cv_language_versions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"master_cv_id", "language_code"}))
public class CvLanguageVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "master_cv_id", nullable = false)
    private UUID masterCvId;

    @Enumerated(EnumType.STRING)
    @Column(name = "language_code", nullable = false, length = 10)
    private LanguageCode languageCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_code", nullable = false, length = 50)
    private TemplateCode templateCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private CvContent content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "section_order", columnDefinition = "jsonb", nullable = false)
    private List<CvSection> sectionOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public CvLanguageVersion() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMasterCvId() {
        return masterCvId;
    }

    public void setMasterCvId(UUID masterCvId) {
        this.masterCvId = masterCvId;
    }

    public LanguageCode getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(LanguageCode languageCode) {
        this.languageCode = languageCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TemplateCode getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(TemplateCode templateCode) {
        this.templateCode = templateCode;
    }

    public CvContent getContent() {
        return content;
    }

    public void setContent(CvContent content) {
        this.content = content;
    }

    public List<CvSection> getSectionOrder() {
        return sectionOrder;
    }

    public void setSectionOrder(List<CvSection> sectionOrder) {
        this.sectionOrder = sectionOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
