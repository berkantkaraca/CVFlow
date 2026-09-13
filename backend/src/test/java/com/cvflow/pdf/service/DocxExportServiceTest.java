package com.cvflow.pdf.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cvflow.common.exception.LanguageVersionNotFoundException;
import com.cvflow.common.security.CvAccessService;
import com.cvflow.cv.dto.request.DocxExportRequest;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import com.cvflow.cv.repository.CvLanguageVersionRepository;
import com.cvflow.pdf.docx.DocxRenderer;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocxExportServiceTest {

    @Mock
    private CvAccessService cvAccessService;

    @Mock
    private CvLanguageVersionRepository languageVersionRepository;

    @Mock
    private DocxRenderer docxRenderer;

    private DocxExportService service;
    private final UUID cvId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        service = new DocxExportService(cvAccessService, languageVersionRepository, docxRenderer);
    }

    @Test
    void exportsUsingVersionContent() {
        MasterCv cv = masterCv();
        CvLanguageVersion version = languageVersion();
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(cv);
        when(languageVersionRepository.findByMasterCvIdAndLanguageCode(cv.getId(), LanguageCode.EN))
                .thenReturn(Optional.of(version));
        when(docxRenderer.render(any(), any(), any())).thenReturn(new byte[] {1, 2, 3});

        DocxExportService.DocxDocument document = service.exportDocx(cvId, new DocxExportRequest(LanguageCode.EN));

        assertEquals("cv-en.docx", document.filename());
        assertEquals(3, document.content().length);
    }

    @Test
    void throwsWhenLanguageVersionMissing() {
        MasterCv cv = masterCv();
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(cv);
        when(languageVersionRepository.findByMasterCvIdAndLanguageCode(cv.getId(), LanguageCode.EN))
                .thenReturn(Optional.empty());

        assertThrows(
                LanguageVersionNotFoundException.class,
                () -> service.exportDocx(cvId, new DocxExportRequest(LanguageCode.EN)));
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
        version.setTemplateCode(TemplateCode.ATS);
        version.setContent(CvContent.empty());
        version.setSectionOrder(CvSection.defaultOrder());
        version.setVersion(0L);
        return version;
    }
}
