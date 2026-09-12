package com.cvflow.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.cvflow.common.exception.AccessDeniedException;
import com.cvflow.common.exception.LanguageVersionNotFoundException;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import com.cvflow.cv.repository.CvLanguageVersionRepository;
import com.cvflow.cv.repository.MasterCvRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CvAccessServiceTest {

    @Mock
    private MasterCvRepository masterCvRepository;

    @Mock
    private CvLanguageVersionRepository languageVersionRepository;

    @Mock
    private CurrentUserService currentUserService;

    private CvAccessService cvAccessService;
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setup() {
        cvAccessService =
                new CvAccessService(masterCvRepository, languageVersionRepository, currentUserService);
        org.mockito.Mockito.lenient()
                .when(currentUserService.getCurrentUserId())
                .thenReturn(userId);
    }

    @Test
    void getOwnedLanguageVersionReturnsVersion() {
        UUID versionId = UUID.randomUUID();
        UUID cvId = UUID.randomUUID();
        when(languageVersionRepository.findById(versionId)).thenReturn(Optional.of(languageVersion(versionId, cvId)));
        when(masterCvRepository.findById(cvId)).thenReturn(Optional.of(masterCv(cvId)));

        assertEquals(versionId, cvAccessService.getOwnedLanguageVersion(versionId).getId());
    }

    @Test
    void getOwnedLanguageVersionHidesForeignResources() {
        UUID versionId = UUID.randomUUID();
        UUID cvId = UUID.randomUUID();
        when(languageVersionRepository.findById(versionId)).thenReturn(Optional.of(languageVersion(versionId, cvId)));
        MasterCv foreign = masterCv(cvId);
        foreign.setUserId(UUID.randomUUID());
        when(masterCvRepository.findById(cvId)).thenReturn(Optional.of(foreign));

        assertThrows(AccessDeniedException.class, () -> cvAccessService.getOwnedLanguageVersion(versionId));
    }

    @Test
    void getOwnedLanguageVersionThrowsWhenMissing() {
        UUID versionId = UUID.randomUUID();
        when(languageVersionRepository.findById(versionId)).thenReturn(Optional.empty());

        assertThrows(LanguageVersionNotFoundException.class, () -> cvAccessService.getOwnedLanguageVersion(versionId));
    }

    private MasterCv masterCv(UUID cvId) {
        MasterCv cv = new MasterCv();
        cv.setId(cvId);
        cv.setUserId(userId);
        cv.setName("CV");
        cv.setContent(CvContent.empty());
        cv.setSectionOrder(CvSection.defaultOrder());
        cv.setVersion(0L);
        return cv;
    }

    private CvLanguageVersion languageVersion(UUID versionId, UUID cvId) {
        CvLanguageVersion version = new CvLanguageVersion();
        version.setId(versionId);
        version.setMasterCvId(cvId);
        version.setLanguageCode(LanguageCode.EN);
        version.setName("English");
        version.setTemplateCode(TemplateCode.MODERN);
        version.setContent(CvContent.empty());
        version.setSectionOrder(CvSection.defaultOrder());
        version.setVersion(0L);
        return version;
    }
}
