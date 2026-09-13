package com.cvflow.pdf.service;

import com.cvflow.common.exception.LanguageVersionNotFoundException;
import com.cvflow.common.security.CvAccessService;
import com.cvflow.cv.dto.request.DocxExportRequest;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.repository.CvLanguageVersionRepository;
import com.cvflow.pdf.docx.DocxRenderer;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocxExportService {
    public record DocxDocument(byte[] content, String filename) {}

    private final CvAccessService cvAccessService;
    private final CvLanguageVersionRepository languageVersionRepository;
    private final DocxRenderer docxRenderer;

    @Transactional(readOnly = true)
    public DocxDocument exportDocx(UUID cvId, DocxExportRequest request) {
        MasterCv cv = cvAccessService.getOwnedCv(cvId);
        CvLanguageVersion version = languageVersionRepository
                .findByMasterCvIdAndLanguageCode(cv.getId(), request.languageCode())
                .orElseThrow(LanguageVersionNotFoundException::new);
        byte[] docx = docxRenderer.render(version.getContent(), version.getSectionOrder(), version.getLanguageCode());
        String filename = "cv-" + request.languageCode().name().toLowerCase() + ".docx";
        return new DocxDocument(docx, filename);
    }
}
