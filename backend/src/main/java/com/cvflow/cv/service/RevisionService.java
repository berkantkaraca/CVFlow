package com.cvflow.cv.service;

import com.cvflow.common.exception.RevisionNotFoundException;
import com.cvflow.cv.dto.response.RevisionResponse;
import com.cvflow.cv.entity.CvContentRevision;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.RevisionDocumentType;
import com.cvflow.cv.repository.CvContentRevisionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RevisionService {
    static final int MAX_REVISIONS = 20;

    private final CvContentRevisionRepository revisionRepository;
    private final CvContentCloner cloner;

    public void captureMaster(MasterCv cv) {
        capture(
                RevisionDocumentType.MASTER_CV,
                cv.getId(),
                cv.getContent(),
                cv.getSectionOrder(),
                cv.getVersion());
    }

    public void captureLanguage(CvLanguageVersion version) {
        capture(
                RevisionDocumentType.LANGUAGE_VERSION,
                version.getId(),
                version.getContent(),
                version.getSectionOrder(),
                version.getVersion());
    }

    @Transactional(readOnly = true)
    public List<RevisionResponse> list(RevisionDocumentType type, UUID documentId) {
        return revisionRepository.findByDocumentTypeAndDocumentIdOrderByCreatedAtDesc(type, documentId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CvContentRevision load(RevisionDocumentType type, UUID documentId, UUID revisionId) {
        CvContentRevision revision =
                revisionRepository.findById(revisionId).orElseThrow(RevisionNotFoundException::new);
        if (revision.getDocumentType() != type || !revision.getDocumentId().equals(documentId)) {
            throw new RevisionNotFoundException();
        }
        return revision;
    }

    public void deleteAll(RevisionDocumentType type, UUID documentId) {
        List<CvContentRevision> revisions =
                revisionRepository.findByDocumentTypeAndDocumentIdOrderByCreatedAtDesc(type, documentId);
        revisionRepository.deleteAll(revisions);
    }

    private void capture(
            RevisionDocumentType type,
            UUID documentId,
            CvContent content,
            List<CvSection> sectionOrder,
            Long version) {
        CvContentRevision revision = new CvContentRevision();
        revision.setDocumentType(type);
        revision.setDocumentId(documentId);
        revision.setContent(cloner.clone(content));
        revision.setSectionOrder(
                sectionOrder == null ? List.of() : new ArrayList<>(sectionOrder));
        revision.setVersion(version);
        revisionRepository.save(revision);
        prune(type, documentId);
    }

    private void prune(RevisionDocumentType type, UUID documentId) {
        List<CvContentRevision> revisions =
                revisionRepository.findByDocumentTypeAndDocumentIdOrderByCreatedAtDesc(type, documentId);
        if (revisions.size() > MAX_REVISIONS) {
            revisionRepository.deleteAll(revisions.subList(MAX_REVISIONS, revisions.size()));
        }
    }

    private RevisionResponse toResponse(CvContentRevision revision) {
        return new RevisionResponse(
                revision.getId(),
                revision.getVersion(),
                revision.getContent(),
                revision.getSectionOrder(),
                revision.getCreatedAt());
    }
}
