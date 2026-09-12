package com.cvflow.cv.dto.request;

import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.TemplateCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateLanguageVersionRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull TemplateCode templateCode,
        @NotNull CvContent content,
        @NotNull List<CvSection> sectionOrder,
        @NotNull Long version) {}
