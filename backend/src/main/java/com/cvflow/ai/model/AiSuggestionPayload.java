package com.cvflow.ai.model;

import com.cvflow.cv.model.CvSection;
import java.util.UUID;

public class AiSuggestionPayload {
    private CvSection section;
    private UUID entityId;
    private String field;
    private String original;
    private String suggested;

    public AiSuggestionPayload() {}

    public AiSuggestionPayload(
            CvSection section, UUID entityId, String field, String original, String suggested) {
        this.section = section;
        this.entityId = entityId;
        this.field = field;
        this.original = original;
        this.suggested = suggested;
    }

    public CvSection getSection() {
        return section;
    }

    public void setSection(CvSection section) {
        this.section = section;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getSuggested() {
        return suggested;
    }

    public void setSuggested(String suggested) {
        this.suggested = suggested;
    }
}
