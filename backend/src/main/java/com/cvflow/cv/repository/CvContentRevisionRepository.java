package com.cvflow.cv.repository;

import com.cvflow.cv.entity.CvContentRevision;
import com.cvflow.cv.model.RevisionDocumentType;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvContentRevisionRepository extends JpaRepository<CvContentRevision, UUID> {
    List<CvContentRevision> findByDocumentTypeAndDocumentIdOrderByCreatedAtDesc(
            RevisionDocumentType documentType, UUID documentId);

    void deleteByDocumentTypeAndDocumentIdIn(RevisionDocumentType documentType, Collection<UUID> documentIds);
}
