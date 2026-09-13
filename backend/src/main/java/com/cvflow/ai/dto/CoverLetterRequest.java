package com.cvflow.ai.dto;

import com.cvflow.cv.model.LanguageCode;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CoverLetterRequest(
        @NotNull UUID masterCvId, @NotNull LanguageCode languageCode, String jobDescription) {}
