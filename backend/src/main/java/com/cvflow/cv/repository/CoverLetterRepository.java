package com.cvflow.cv.repository;

import com.cvflow.cv.entity.CoverLetter;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoverLetterRepository extends JpaRepository<CoverLetter, UUID> {
    List<CoverLetter> findAllByMasterCvIdOrderByUpdatedAtDesc(UUID masterCvId);
}
