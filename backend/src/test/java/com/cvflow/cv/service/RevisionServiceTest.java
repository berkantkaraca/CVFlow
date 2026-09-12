package com.cvflow.cv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cvflow.common.exception.RevisionNotFoundException;
import com.cvflow.cv.entity.CvContentRevision;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.RevisionDocumentType;
import com.cvflow.cv.repository.CvContentRevisionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class RevisionServiceTest {

    @Mock
    private CvContentRevisionRepository revisionRepository;

    private RevisionService service;
    private final UUID documentId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        service = new RevisionService(revisionRepository, new CvContentCloner(new ObjectMapper()));
    }

    @Test
    void captureStoresDeepCopy() {
        MasterCv cv = masterCv();
        when(revisionRepository.findByDocumentTypeAndDocumentIdOrderByCreatedAtDesc(
                        RevisionDocumentType.MASTER_CV, documentId))
                .thenReturn(List.of());

        service.captureMaster(cv);
        cv.getContent().setSummary("mutated after capture");

        ArgumentCaptor<CvContentRevision> captor = ArgumentCaptor.forClass(CvContentRevision.class);
        verify(revisionRepository).save(captor.capture());
        assertEquals("original", captor.getValue().getContent().getSummary());
        assertEquals(0L, captor.getValue().getVersion());
    }

    @Test
    void pruneKeepsLatestTwenty() {
        MasterCv cv = masterCv();
        List<CvContentRevision> existing = IntStream.range(0, 20)
                .mapToObj(i -> {
                    CvContentRevision revision = new CvContentRevision();
                    revision.setId(UUID.randomUUID());
                    return revision;
                })
                .toList();
        when(revisionRepository.findByDocumentTypeAndDocumentIdOrderByCreatedAtDesc(
                        RevisionDocumentType.MASTER_CV, documentId))
                .thenReturn(existing);

        service.captureMaster(cv);

        verify(revisionRepository, never()).deleteAll(any());
    }

    @Test
    void pruneDeletesBeyondTwenty() {
        MasterCv cv = masterCv();
        List<CvContentRevision> existing = IntStream.range(0, 26)
                .mapToObj(i -> {
                    CvContentRevision revision = new CvContentRevision();
                    revision.setId(UUID.randomUUID());
                    return revision;
                })
                .toList();
        when(revisionRepository.findByDocumentTypeAndDocumentIdOrderByCreatedAtDesc(
                        RevisionDocumentType.MASTER_CV, documentId))
                .thenReturn(existing);

        service.captureMaster(cv);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CvContentRevision>> captor = ArgumentCaptor.forClass(List.class);
        verify(revisionRepository).deleteAll(captor.capture());
        assertEquals(6, captor.getValue().size());
    }

    @Test
    void loadRejectsForeignDocument() {
        CvContentRevision revision = new CvContentRevision();
        revision.setId(UUID.randomUUID());
        revision.setDocumentType(RevisionDocumentType.MASTER_CV);
        revision.setDocumentId(UUID.randomUUID());
        when(revisionRepository.findById(revision.getId())).thenReturn(Optional.of(revision));

        assertThrows(
                RevisionNotFoundException.class,
                () -> service.load(RevisionDocumentType.MASTER_CV, documentId, revision.getId()));
    }

    @Test
    void loadThrowsWhenMissing() {
        UUID revisionId = UUID.randomUUID();
        when(revisionRepository.findById(revisionId)).thenReturn(Optional.empty());

        assertThrows(
                RevisionNotFoundException.class,
                () -> service.load(RevisionDocumentType.MASTER_CV, documentId, revisionId));
    }

    private MasterCv masterCv() {
        CvContent content = CvContent.empty();
        content.setSummary("original");
        MasterCv cv = new MasterCv();
        cv.setId(documentId);
        cv.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        cv.setName("CV");
        cv.setContent(content);
        cv.setSectionOrder(new ArrayList<>(CvSection.defaultOrder()));
        cv.setVersion(0L);
        return cv;
    }
}
