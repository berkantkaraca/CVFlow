package com.cvflow.cv.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CoverLetterSummaryResponse(UUID id, String title, Instant updatedAt, Long version) {}
