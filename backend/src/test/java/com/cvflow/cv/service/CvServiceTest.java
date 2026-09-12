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
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
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
    private CurrentUserService currentUserService;

    @Mock
    private CvAccessService cvAccessService;

    private CvService cvService;
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID cvId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        cvService = new CvService(
                masterCvRepository, currentUserService, cvAccessService, new CvContentCloner(new ObjectMapper()));
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
        when(masterCvRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        CvResponse copy = cvService.duplicate(cvId);

        assertEquals("Original (Copy)", copy.name());
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
}
