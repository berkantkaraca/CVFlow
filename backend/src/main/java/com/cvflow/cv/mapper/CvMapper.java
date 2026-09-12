package com.cvflow.cv.mapper;

import com.cvflow.cv.dto.response.CvResponse;
import com.cvflow.cv.dto.response.CvSummaryResponse;
import com.cvflow.cv.entity.MasterCv;

public final class CvMapper {
    private CvMapper() {}

    public static CvSummaryResponse toSummary(MasterCv cv) {
        return new CvSummaryResponse(
                cv.getId(),
                cv.getName(),
                cv.getCreatedAt(),
                cv.getUpdatedAt(),
                cv.getVersion());
    }

    public static CvResponse toResponse(MasterCv cv) {
        return new CvResponse(
                cv.getId(),
                cv.getName(),
                cv.getContent(),
                cv.getSectionOrder(),
                cv.getCreatedAt(),
                cv.getUpdatedAt(),
                cv.getVersion());
    }
}
