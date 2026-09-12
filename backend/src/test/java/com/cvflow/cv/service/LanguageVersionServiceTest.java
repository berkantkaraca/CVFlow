package com.cvflow.cv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cvflow.common.exception.LanguageVersionAlreadyExistsException;
import com.cvflow.common.exception.LanguageVersionNotFoundException;
import com.cvflow.common.exception.OptimisticLockException;
import com.cvflow.common.security.CvAccessService;
import com.cvflow.cv.dto.request.CreateLanguageVersionRequest;
import com.cvflow.cv.dto.request.UpdateLanguageVersionRequest;
import com.cvflow.cv.dto.response.LanguageVersionResponse;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import com.cvflow.cv.repository.CvLanguageVersionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class LanguageVersionServiceTest {

    @Mock
    private CvLanguageVersionRepository languageVersionRepository;

    @Mock
    private CvAccessService cvAccessService;

    private LanguageVersionService service;
    private final UUID cvId = UUID.randomUUID();
    private final UUID versionId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        service = new LanguageVersionService(
                languageVersionRepository,
                cvAccessService,
                new CvContentCloner(new ObjectMapper()),
                new SectionOrderValidator());
    }

    @Test
    void createCopiesMasterContent() {
        MasterCv cv = masterCv();
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(cv);
        when(languageVersionRepository.findByMasterCvIdAndLanguageCode(cvId, LanguageCode.EN))
                .thenReturn(Optional.empty());
        when(languageVersionRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        LanguageVersionResponse response =
                service.create(cvId, new CreateLanguageVersionRequest(LanguageCode.EN, "English", TemplateCode.MODERN));

        assertEquals(LanguageCode.EN, response.languageCode());
        assertEquals(cvId, response.masterCvId());
        verify(languageVersionRepository).saveAndFlush(any());
    }

    @Test
    void createRejectsDuplicateLanguage() {
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(masterCv());
        when(languageVersionRepository.findByMasterCvIdAndLanguageCode(cvId, LanguageCode.EN))
                .thenReturn(Optional.of(languageVersion()));

        assertThrows(
                LanguageVersionAlreadyExistsException.class,
                () -> service.create(cvId, new CreateLanguageVersionRequest(LanguageCode.EN, "English", TemplateCode.MODERN)));
    }

    @Test
    void getRejectsForeignCv() {
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(masterCv());
        CvLanguageVersion other = languageVersion();
        other.setMasterCvId(UUID.randomUUID());
        when(cvAccessService.getOwnedLanguageVersion(versionId)).thenReturn(other);

        assertThrows(LanguageVersionNotFoundException.class, () -> service.get(cvId, versionId));
    }

    @Test
    void updateRejectsStaleVersion() {
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(masterCv());
        when(cvAccessService.getOwnedLanguageVersion(versionId)).thenReturn(languageVersion());

        UpdateLanguageVersionRequest request = new UpdateLanguageVersionRequest(
                "English", TemplateCode.MINIMAL, CvContent.empty(), CvSection.defaultOrder(), 4L);

        assertThrows(OptimisticLockException.class, () -> service.update(cvId, versionId, request));
    }

    @Test
    void listDelegatesToRepository() {
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(masterCv());
        when(languageVersionRepository.findAllByMasterCvId(cvId)).thenReturn(List.of(languageVersion()));

        assertEquals(1, service.list(cvId).size());
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
        version.setId(versionId);
        version.setMasterCvId(cvId);
        version.setLanguageCode(LanguageCode.EN);
        version.setName("English");
        version.setTemplateCode(TemplateCode.MODERN);
        version.setContent(CvContent.empty());
        version.setSectionOrder(CvSection.defaultOrder());
        version.setVersion(5L);
        return version;
    }
}
