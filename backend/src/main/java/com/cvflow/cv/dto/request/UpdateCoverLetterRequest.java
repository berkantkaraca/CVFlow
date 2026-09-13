package com.cvflow.cv.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCoverLetterRequest(
        @NotBlank @Size(max = 150) String title,
        @Size(max = 255) String recipient,
        @Size(max = 255) String company,
        @Size(max = 255) String position,
        @NotNull String body,
        @NotNull Long version) {}
