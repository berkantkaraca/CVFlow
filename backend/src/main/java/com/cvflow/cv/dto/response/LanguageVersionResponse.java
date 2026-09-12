package com.cvflow.cv.dto.response;

import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LanguageVersionResponse(
        UUID id,
        UUID masterCvId,
        LanguageCode languageCode,
        String name,
        TemplateCode templateCode,
        CvContent content,
        List<CvSection> sectionOrder,
        Instant createdAt,
        Instant updatedAt,
        Long version) {}
