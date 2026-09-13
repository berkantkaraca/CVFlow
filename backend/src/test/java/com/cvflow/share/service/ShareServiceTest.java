package com.cvflow.share.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cvflow.common.exception.ShareNotFoundException;
import com.cvflow.common.security.CvAccessService;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import com.cvflow.share.dto.ShareDtos.CreateShareRequest;
import com.cvflow.share.dto.ShareDtos.ShareResponse;
import com.cvflow.share.entity.ShareLink;
import com.cvflow.share.repository.ShareLinkRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShareServiceTest {

    @Mock
    private ShareLinkRepository shareLinkRepository;

    @Mock
    private CvAccessService cvAccessService;

    private ShareService service;
    private final UUID cvId = UUID.randomUUID();
    private final UUID versionId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        service = new ShareService(shareLinkRepository, cvAccessService);
    }

    @Test
    void createGeneratesOpaqueToken() {
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(masterCv());
        when(cvAccessService.getOwnedLanguageVersion(versionId)).thenReturn(version());
        when(shareLinkRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ShareResponse response = service.create(cvId, versionId, new CreateShareRequest(30));

        assertNotNull(response.token());
        assertTrue(response.token().length() >= 32);
        assertNotNull(response.expiresAt());
    }

    @Test
    void resolveRejectsExpiredLink() {
        ShareLink link = new ShareLink();
        link.setId(UUID.randomUUID());
        link.setToken("token");
        link.setMasterCvId(cvId);
        link.setLanguageVersionId(versionId);
        link.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));
        when(shareLinkRepository.findByToken("token")).thenReturn(Optional.of(link));

        assertThrows(ShareNotFoundException.class, () -> service.resolvePublic("token"));
    }

    @Test
    void resolveRejectsUnknownToken() {
        when(shareLinkRepository.findByToken("nope")).thenReturn(Optional.empty());

        assertThrows(ShareNotFoundException.class, () -> service.resolvePublic("nope"));
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

    private CvLanguageVersion version() {
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
