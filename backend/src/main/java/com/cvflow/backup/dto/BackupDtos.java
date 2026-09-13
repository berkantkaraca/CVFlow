package com.cvflow.backup.dto;

import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class BackupDtos {
    private BackupDtos() {}

    public record BackupDocument(
            int format,
            Instant exportedAt,
            String email,
            @Valid List<BackupMasterCv> masterCvs) {}

    public record BackupMasterCv(
            @NotBlank @Size(max = 150) String name,
            @NotNull CvContent content,
            @NotNull List<CvSection> sectionOrder,
            @Valid List<BackupLanguageVersion> languages) {}

    public record BackupLanguageVersion(
            @NotNull LanguageCode languageCode,
            @NotBlank @Size(max = 150) String name,
            @NotNull TemplateCode templateCode,
            @NotNull CvContent content,
            @NotNull List<CvSection> sectionOrder) {}

    public record BackupImportResult(int importedCvs, int importedLanguages) {}
}
