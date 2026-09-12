package com.cvflow.ai.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AcceptAllRequest(@NotNull UUID languageVersionId) {}
