package com.cvflow.pdf.service;

import com.cvflow.common.exception.LanguageVersionNotFoundException;
import com.cvflow.common.security.CvAccessService;
import com.cvflow.cv.dto.request.PdfExportRequest;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.repository.CvLanguageVersionRepository;
import com.cvflow.pdf.template.TemplateRenderer;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PdfExportService {
    public record PdfDocument(byte[] content, String filename) {}

    private final CvAccessService cvAccessService;
    private final CvLanguageVersionRepository languageVersionRepository;
    private final TemplateRenderer templateRenderer;
    private final GotenbergPdfClient pdfClient;

    public PdfExportService(
            CvAccessService cvAccessService,
            CvLanguageVersionRepository languageVersionRepository,
            TemplateRenderer templateRenderer,
            GotenbergPdfClient pdfClient) {
        this.cvAccessService = cvAccessService;
        this.languageVersionRepository = languageVersionRepository;
        this.templateRenderer = templateRenderer;
        this.pdfClient = pdfClient;
    }

    @Transactional(readOnly = true)
    public PdfDocument exportPdf(UUID cvId, PdfExportRequest request) {
        MasterCv cv = cvAccessService.getOwnedCv(cvId);
        CvLanguageVersion version = languageVersionRepository
                .findByMasterCvIdAndLanguageCode(cv.getId(), request.languageCode())
                .orElseThrow(LanguageVersionNotFoundException::new);
        String html = templateRenderer.render(
                version.getContent(),
                version.getSectionOrder(),
                version.getTemplateCode(),
                version.getLanguageCode());
        byte[] pdf = pdfClient.convertHtmlToPdf(html);
        String filename = "cv-" + request.languageCode().name().toLowerCase() + ".pdf";
        return new PdfDocument(pdf, filename);
    }
}
