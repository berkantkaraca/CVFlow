package com.cvflow.pdf.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cvflow.common.exception.LanguageVersionNotFoundException;
import com.cvflow.common.security.CvAccessService;
import com.cvflow.cv.dto.request.PdfExportRequest;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import com.cvflow.cv.repository.CvLanguageVersionRepository;
import com.cvflow.pdf.template.TemplateRenderer;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PdfExportServiceTest {

    @Mock
    private CvAccessService cvAccessService;

    @Mock
    private CvLanguageVersionRepository languageVersionRepository;

    @Mock
    private TemplateRenderer templateRenderer;

    @Mock
    private GotenbergPdfClient pdfClient;

    private PdfExportService service;
    private final UUID cvId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        service = new PdfExportService(cvAccessService, languageVersionRepository, templateRenderer, pdfClient);
    }

    @Test
    void exportsUsingVersionTemplate() {
        MasterCv cv = masterCv();
        CvLanguageVersion version = languageVersion();
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(cv);
        when(languageVersionRepository.findByMasterCvIdAndLanguageCode(cv.getId(), LanguageCode.EN))
                .thenReturn(Optional.of(version));
        when(templateRenderer.render(any(), any(), any(), any())).thenReturn("<html></html>");
        when(pdfClient.convertHtmlToPdf("<html></html>")).thenReturn(new byte[] {1, 2, 3});

        PdfExportService.PdfDocument document = service.exportPdf(cvId, new PdfExportRequest(LanguageCode.EN));

        assertEquals("cv-en.pdf", document.filename());
        assertEquals(3, document.content().length);
        verify(templateRenderer)
                .render(
                        version.getContent(),
                        version.getSectionOrder(),
                        TemplateCode.PROFESSIONAL,
                        LanguageCode.EN);
    }

    @Test
    void throwsWhenLanguageVersionMissing() {
        MasterCv cv = masterCv();
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(cv);
        when(languageVersionRepository.findByMasterCvIdAndLanguageCode(cv.getId(), LanguageCode.EN))
                .thenReturn(Optional.empty());

        assertThrows(
                LanguageVersionNotFoundException.class,
                () -> service.exportPdf(cvId, new PdfExportRequest(LanguageCode.EN)));
    }

    private MasterCv masterCv() {
        MasterCv cv = new MasterCv();
        cv.setId(cvId);
        cv.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        cv.setName("CV");
        cv.setContent(CvContent.empty());
        cv.setSectionOrder(CvSection.defaultOrder());
        cv.setVersion(0L);
        return cv;
    }

    private CvLanguageVersion languageVersion() {
        CvLanguageVersion version = new CvLanguageVersion();
        version.setId(UUID.randomUUID());
        version.setMasterCvId(cvId);
        version.setLanguageCode(LanguageCode.EN);
        version.setName("English");
        version.setTemplateCode(TemplateCode.PROFESSIONAL);
        version.setContent(CvContent.empty());
        version.setSectionOrder(CvSection.defaultOrder());
        version.setVersion(0L);
        return version;
    }
}
