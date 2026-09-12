package com.cvflow.cv.dto.request;

import jakarta.validation.constraints.NotNull;

public record RestoreRevisionRequest(@NotNull Long version) {}
