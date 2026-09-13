package com.cvflow.cv.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCoverLetterRequest(
        @NotNull com.cvflow.cv.model.LanguageCode languageCode,
        @NotBlank @Size(max = 150) String title,
        @Size(max = 255) String recipient,
        @Size(max = 255) String company,
        @Size(max = 255) String position) {}
