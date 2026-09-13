package com.cvflow.share.service;

import com.cvflow.common.exception.LanguageVersionNotFoundException;
import com.cvflow.common.exception.ShareNotFoundException;
import com.cvflow.common.security.CvAccessService;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.share.dto.ShareDtos.CreateShareRequest;
import com.cvflow.share.dto.ShareDtos.ShareResponse;
import com.cvflow.share.entity.ShareLink;
import com.cvflow.share.repository.ShareLinkRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShareService {
    private final ShareLinkRepository shareLinkRepository;
    private final CvAccessService cvAccessService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public ShareResponse create(UUID cvId, UUID languageVersionId, CreateShareRequest request) {
        CvLanguageVersion version = ownedVersion(cvId, languageVersionId);
        ShareLink link = new ShareLink();
        link.setToken(newToken());
        link.setMasterCvId(version.getMasterCvId());
        link.setLanguageVersionId(version.getId());
        if (request != null && request.expiresInDays() != null) {
            link.setExpiresAt(Instant.now().plus(request.expiresInDays(), ChronoUnit.DAYS));
        }
        ShareLink saved = shareLinkRepository.save(link);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ShareResponse> list(UUID cvId, UUID languageVersionId) {
        CvLanguageVersion version = ownedVersion(cvId, languageVersionId);
        return shareLinkRepository.findAllByLanguageVersionIdOrderByCreatedAtDesc(version.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void revoke(UUID cvId, UUID languageVersionId, UUID shareId) {
        CvLanguageVersion version = ownedVersion(cvId, languageVersionId);
        ShareLink link = shareLinkRepository
                .findById(shareId)
                .filter(candidate -> candidate.getLanguageVersionId().equals(version.getId()))
                .orElseThrow(ShareNotFoundException::new);
        shareLinkRepository.delete(link);
    }

    @Transactional(readOnly = true)
    public ShareLink resolvePublic(String token) {
        ShareLink link = shareLinkRepository
                .findByToken(token)
                .orElseThrow(ShareNotFoundException::new);
        if (link.getExpiresAt() != null && Instant.now().isAfter(link.getExpiresAt())) {
            throw new ShareNotFoundException();
        }
        return link;
    }

    private CvLanguageVersion ownedVersion(UUID cvId, UUID languageVersionId) {
        MasterCv cv = cvAccessService.getOwnedCv(cvId);
        CvLanguageVersion version = cvAccessService.getOwnedLanguageVersion(languageVersionId);
        if (!version.getMasterCvId().equals(cv.getId())) {
            throw new LanguageVersionNotFoundException();
        }
        return version;
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private ShareResponse toResponse(ShareLink link) {
        return new ShareResponse(link.getId(), link.getToken(), link.getExpiresAt(), link.getCreatedAt());
    }
}
