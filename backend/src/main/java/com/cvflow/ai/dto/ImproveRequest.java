package com.cvflow.ai.dto;

import com.cvflow.cv.model.CvSection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ImproveRequest(
        @NotNull UUID languageVersionId,
        @NotNull CvSection section,
        UUID entityId,
        @NotBlank String field,
        @NotBlank String content) {}
