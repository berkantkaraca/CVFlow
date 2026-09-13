package com.cvflow.billing.repository;

import com.cvflow.billing.entity.AiUsage;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiUsageRepository extends JpaRepository<AiUsage, UUID> {
    long countByUserIdAndCreatedAtAfter(UUID userId, Instant after);
}
