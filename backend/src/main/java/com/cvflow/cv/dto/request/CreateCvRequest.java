package com.cvflow.cv.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCvRequest(
        @NotBlank @Size(max = 150) String name) {}
