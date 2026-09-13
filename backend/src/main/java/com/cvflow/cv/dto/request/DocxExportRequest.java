package com.cvflow.cv.dto.request;

import com.cvflow.cv.model.LanguageCode;
import jakarta.validation.constraints.NotNull;

public record DocxExportRequest(@NotNull LanguageCode languageCode) {}
