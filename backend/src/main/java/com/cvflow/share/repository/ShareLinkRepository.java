package com.cvflow.share.repository;

import com.cvflow.share.entity.ShareLink;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareLinkRepository extends JpaRepository<ShareLink, UUID> {
    Optional<ShareLink> findByToken(String token);

    List<ShareLink> findAllByLanguageVersionIdOrderByCreatedAtDesc(UUID languageVersionId);
}
