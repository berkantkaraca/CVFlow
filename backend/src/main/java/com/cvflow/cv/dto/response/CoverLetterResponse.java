package com.cvflow.cv.dto.response;

import com.cvflow.cv.model.LanguageCode;
import java.time.Instant;
import java.util.UUID;

public record CoverLetterResponse(
        UUID id,
        UUID masterCvId,
        LanguageCode languageCode,
        String title,
        String recipient,
        String company,
        String position,
        String body,
        Instant createdAt,
        Instant updatedAt,
        Long version) {}
