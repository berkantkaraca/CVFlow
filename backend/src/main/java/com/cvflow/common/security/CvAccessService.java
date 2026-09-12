package com.cvflow.common.security;

import com.cvflow.common.exception.AccessDeniedException;
import com.cvflow.common.exception.CvNotFoundException;
import com.cvflow.common.exception.LanguageVersionNotFoundException;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.repository.CvLanguageVersionRepository;
import com.cvflow.cv.repository.MasterCvRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CvAccessService {
    private final MasterCvRepository masterCvRepository;
    private final CvLanguageVersionRepository languageVersionRepository;
    private final CurrentUserService currentUserService;

    public CvAccessService(
            MasterCvRepository masterCvRepository,
            CvLanguageVersionRepository languageVersionRepository,
            CurrentUserService currentUserService) {
        this.masterCvRepository = masterCvRepository;
        this.languageVersionRepository = languageVersionRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public MasterCv getOwnedCv(UUID cvId) {
        MasterCv cv = masterCvRepository.findById(cvId).orElseThrow(CvNotFoundException::new);
        if (!cv.getUserId().equals(currentUserService.getCurrentUserId())) {
            throw new AccessDeniedException();
        }
        return cv;
    }

    @Transactional(readOnly = true)
    public CvLanguageVersion getOwnedLanguageVersion(UUID languageVersionId) {
        CvLanguageVersion version = languageVersionRepository
                .findById(languageVersionId)
                .orElseThrow(LanguageVersionNotFoundException::new);
        getOwnedCv(version.getMasterCvId());
        return version;
    }
}
