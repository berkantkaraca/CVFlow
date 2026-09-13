package com.cvflow.cv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cvflow.common.exception.AccessDeniedException;
import com.cvflow.common.exception.CvNotFoundException;
import com.cvflow.common.exception.InvalidSectionOrderException;
import com.cvflow.common.exception.OptimisticLockException;
import com.cvflow.common.security.CvAccessService;
import com.cvflow.common.security.CurrentUserService;
import com.cvflow.cv.dto.request.CreateCvRequest;
import com.cvflow.cv.dto.request.UpdateCvRequest;
import com.cvflow.cv.dto.response.CvResponse;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import com.cvflow.cv.repository.CvLanguageVersionRepository;
import com.cvflow.cv.repository.MasterCvRepository;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CvServiceTest {

    @Mock
    private MasterCvRepository masterCvRepository;

    @Mock
    private CvLanguageVersionRepository languageVersionRepository;

    @Mock
    private RevisionService revisionService;

    @Mock
    private com.cvflow.billing.service.UsageService usageService;

    @Mock
    private com.cvflow.cv.repository.CvContentRevisionRepository revisionContentRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private CvAccessService cvAccessService;

    private CvService cvService;
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID cvId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        cvService = new CvService(
                masterCvRepository,
                languageVersionRepository,
                currentUserService,
                cvAccessService,
                new CvContentCloner(new ObjectMapper()),
                new SectionOrderValidator(),
                revisionService,
                usageService,
                revisionContentRepository);
        lenient().when(currentUserService.getCurrentUserId()).thenReturn(userId);
    }

    @Test
    void createUsesEmptyContentAndDefaultOrder() {
        when(masterCvRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        CvResponse response = cvService.create(new CreateCvRequest("Backend CV"));

        assertEquals("Backend CV", response.name());
        assertEquals(CvSection.defaultOrder(), response.sectionOrder());
        verify(masterCvRepository).saveAndFlush(any());
    }

    @Test
    void updateRejectsStaleVersion() {
        MasterCv cv = ownedCv(5L);
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(cv);

        UpdateCvRequest request = new UpdateCvRequest(
                "New", CvContent.empty(), CvSection.defaultOrder(), 4L);

        assertThrows(OptimisticLockException.class, () -> cvService.update(cvId, request));
    }

    @Test
    void updateRejectsDuplicateSections() {
        MasterCv cv = ownedCv(5L);
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(cv);

        UpdateCvRequest request = new UpdateCvRequest(
                "New", CvContent.empty(), List.of(CvSection.SUMMARY, CvSection.SUMMARY), 5L);

        assertThrows(InvalidSectionOrderException.class, () -> cvService.update(cvId, request));
    }

    @Test
    void getPropagatesNotFound() {
        when(cvAccessService.getOwnedCv(cvId)).thenThrow(new CvNotFoundException());

        assertThrows(CvNotFoundException.class, () -> cvService.get(cvId));
    }

    @Test
    void getPropagatesAccessDenied() {
        when(cvAccessService.getOwnedCv(cvId)).thenThrow(new AccessDeniedException());

        assertThrows(AccessDeniedException.class, () -> cvService.get(cvId));
    }

    @Test
    void duplicateCopiesContentWithNewName() {
        MasterCv source = ownedCv(2L);
        source.setName("Original");
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(source);
        when(languageVersionRepository.findAllByMasterCvId(cvId)).thenReturn(List.of());
        when(masterCvRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        CvResponse copy = cvService.duplicate(cvId);

        assertEquals("Original (Copy)", copy.name());
    }

    @Test
    void duplicateCopiesLanguageVersions() {        MasterCv source = ownedCv(2L);
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(source);
        CvLanguageVersion english = languageVersion(LanguageCode.EN);
        when(languageVersionRepository.findAllByMasterCvId(cvId)).thenReturn(List.of(english));
        when(masterCvRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        cvService.duplicate(cvId);

        verify(languageVersionRepository).saveAll(any());
    }

    @Test
    void updateCapturesRevisionBeforeMutating() {
        MasterCv cv = ownedCv(5L);
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(cv);
        when(masterCvRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        UpdateCvRequest request = new UpdateCvRequest(
                "New", CvContent.empty(), CvSection.defaultOrder(), 5L);
        cvService.update(cvId, request);

        verify(revisionService).captureMaster(cv);
    }

    @Test
    void restoreAppliesRevisionContent() {
        MasterCv cv = ownedCv(2L);
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(cv);
        com.cvflow.cv.entity.CvContentRevision revision = new com.cvflow.cv.entity.CvContentRevision();
        revision.setId(UUID.randomUUID());
        CvContent oldContent = CvContent.empty();
        oldContent.setSummary("old summary");
        revision.setContent(oldContent);
        revision.setSectionOrder(List.of(CvSection.SUMMARY));
        when(revisionService.load(
                        com.cvflow.cv.model.RevisionDocumentType.MASTER_CV, cvId, revision.getId()))
                .thenReturn(revision);
        when(masterCvRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        CvResponse restored = cvService.restoreRevision(
                cvId, revision.getId(), new com.cvflow.cv.dto.request.RestoreRevisionRequest(2L));

        assertEquals("old summary", restored.content().getSummary());
        verify(revisionService).captureMaster(cv);
    }

    @Test
    void bulkDeleteReportsPerItemResults() {
        UUID missing = UUID.randomUUID();
        UUID foreignId = UUID.randomUUID();
        MasterCv mine = ownedCv(0L);
        MasterCv foreign = ownedCv(0L);
        foreign.setId(foreignId);
        foreign.setUserId(UUID.randomUUID());
        when(masterCvRepository.findAllById(any())).thenReturn(List.of(mine, foreign));

        var result = cvService.bulkDelete(new com.cvflow.cv.dto.request.BulkDeleteRequest(List.of(cvId, missing, foreignId)));

        assertEquals(List.of(cvId), result.deleted());
        assertEquals(2, result.failed().size());
        assertEquals("CV_NOT_FOUND", result.failed().get(0).code());
        assertEquals("ACCESS_DENIED", result.failed().get(1).code());
        verify(masterCvRepository).deleteAllByIdInBatch(List.of(cvId));
    }

    private MasterCv ownedCv(long version) {
        MasterCv cv = new MasterCv();
        cv.setId(cvId);
        cv.setUserId(userId);
        cv.setName("CV");
        cv.setContent(CvContent.empty());
        cv.setSectionOrder(CvSection.defaultOrder());
        cv.setVersion(version);
        return cv;
    }

    private CvLanguageVersion languageVersion(LanguageCode languageCode) {
        CvLanguageVersion version = new CvLanguageVersion();
        version.setId(UUID.randomUUID());
        version.setMasterCvId(cvId);
        version.setLanguageCode(languageCode);
        version.setName("English");
        version.setTemplateCode(TemplateCode.MODERN);
        version.setContent(CvContent.empty());
        version.setSectionOrder(CvSection.defaultOrder());
        version.setVersion(0L);
        return version;
    }
}
