package com.cvflow.cv.dto.request;

import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateCvRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull CvContent content,
        @NotNull List<CvSection> sectionOrder,
        @NotNull Long version) {}
