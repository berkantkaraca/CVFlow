package com.cvflow.cv.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CvSummaryResponse(
        UUID id,
        String name,
        Instant createdAt,
        Instant updatedAt,
        Long version) {}
