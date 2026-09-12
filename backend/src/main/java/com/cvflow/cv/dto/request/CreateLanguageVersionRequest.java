package com.cvflow.cv.dto.request;

import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLanguageVersionRequest(
        @NotNull LanguageCode languageCode,
        @NotBlank @Size(max = 150) String name,
        @NotNull TemplateCode templateCode) {}
