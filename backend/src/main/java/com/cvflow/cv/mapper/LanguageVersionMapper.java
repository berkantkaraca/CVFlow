package com.cvflow.cv.mapper;

import com.cvflow.cv.dto.response.LanguageVersionResponse;
import com.cvflow.cv.dto.response.LanguageVersionSummaryResponse;
import com.cvflow.cv.entity.CvLanguageVersion;

public final class LanguageVersionMapper {
    private LanguageVersionMapper() {}

    public static LanguageVersionSummaryResponse toSummary(CvLanguageVersion version) {
        return new LanguageVersionSummaryResponse(
                version.getId(),
                version.getMasterCvId(),
                version.getLanguageCode(),
                version.getName(),
                version.getTemplateCode(),
                version.getUpdatedAt(),
                version.getVersion());
    }

    public static LanguageVersionResponse toResponse(CvLanguageVersion version) {
        return new LanguageVersionResponse(
                version.getId(),
                version.getMasterCvId(),
                version.getLanguageCode(),
                version.getName(),
                version.getTemplateCode(),
                version.getContent(),
                version.getSectionOrder(),
                version.getCreatedAt(),
                version.getUpdatedAt(),
                version.getVersion());
    }
}
