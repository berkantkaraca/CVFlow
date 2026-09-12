package com.cvflow.cv.dto.response;

import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import java.time.Instant;
import java.util.UUID;

public record LanguageVersionSummaryResponse(
        UUID id,
        UUID masterCvId,
        LanguageCode languageCode,
        String name,
        TemplateCode templateCode,
        Instant updatedAt,
        Long version) {}
