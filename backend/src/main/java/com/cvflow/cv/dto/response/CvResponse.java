package com.cvflow.cv.dto.response;

import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CvResponse(
        UUID id,
        String name,
        CvContent content,
        List<CvSection> sectionOrder,
        Instant createdAt,
        Instant updatedAt,
        Long version) {}
