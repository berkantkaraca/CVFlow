package com.cvflow.cv.repository;

import com.cvflow.cv.entity.MasterCv;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MasterCvRepository extends JpaRepository<MasterCv, UUID> {
    List<MasterCv> findAllByUserIdOrderByUpdatedAtDesc(UUID userId);
}
