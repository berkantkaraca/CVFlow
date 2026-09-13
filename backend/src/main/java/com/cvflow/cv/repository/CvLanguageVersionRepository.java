package com.cvflow.cv.repository;

import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.model.LanguageCode;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvLanguageVersionRepository extends JpaRepository<CvLanguageVersion, UUID> {
    List<CvLanguageVersion> findAllByMasterCvId(UUID masterCvId);

    List<CvLanguageVersion> findAllByMasterCvIdIn(Collection<UUID> masterCvIds);

    Optional<CvLanguageVersion> findByMasterCvIdAndLanguageCode(UUID masterCvId, LanguageCode languageCode);
}
