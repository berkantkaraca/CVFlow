package com.cvflow.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TailorRequest(@NotNull UUID languageVersionId, @NotBlank String jobDescription) {}
