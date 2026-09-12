package com.cvflow.cv.service;

import com.cvflow.common.exception.LanguageVersionAlreadyExistsException;
import com.cvflow.common.exception.LanguageVersionNotFoundException;
import com.cvflow.common.exception.OptimisticLockException;
import com.cvflow.common.security.CvAccessService;
import com.cvflow.cv.dto.request.CreateLanguageVersionRequest;
import com.cvflow.cv.dto.request.RestoreRevisionRequest;
import com.cvflow.cv.dto.request.UpdateLanguageVersionRequest;
import com.cvflow.cv.dto.response.LanguageVersionResponse;
import com.cvflow.cv.dto.response.LanguageVersionSummaryResponse;
import com.cvflow.cv.dto.response.RevisionResponse;
import com.cvflow.cv.entity.CvContentRevision;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.mapper.LanguageVersionMapper;
import com.cvflow.cv.model.RevisionDocumentType;
import com.cvflow.cv.repository.CvLanguageVersionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LanguageVersionService {
    private final CvLanguageVersionRepository languageVersionRepository;
    private final CvAccessService cvAccessService;
    private final CvContentCloner cloner;
    private final SectionOrderValidator sectionOrderValidator;
    private final RevisionService revisionService;

    @Transactional(readOnly = true)
    public List<LanguageVersionSummaryResponse> list(UUID cvId) {
        MasterCv cv = cvAccessService.getOwnedCv(cvId);
        return languageVersionRepository.findAllByMasterCvId(cv.getId()).stream()
                .map(LanguageVersionMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public LanguageVersionResponse get(UUID cvId, UUID languageVersionId) {
        return LanguageVersionMapper.toResponse(getOwnedInCv(cvId, languageVersionId));
    }

    @Transactional
    public LanguageVersionResponse create(UUID cvId, CreateLanguageVersionRequest request) {
        MasterCv cv = cvAccessService.getOwnedCv(cvId);
        if (languageVersionRepository
                .findByMasterCvIdAndLanguageCode(cv.getId(), request.languageCode())
                .isPresent()) {
            throw new LanguageVersionAlreadyExistsException();
        }
        CvLanguageVersion version = new CvLanguageVersion();
        version.setMasterCvId(cv.getId());
        version.setLanguageCode(request.languageCode());
        version.setName(request.name());
        version.setTemplateCode(request.templateCode());
        version.setContent(cloner.clone(cv.getContent()));
        version.setSectionOrder(new ArrayList<>(cv.getSectionOrder()));
        return LanguageVersionMapper.toResponse(languageVersionRepository.saveAndFlush(version));
    }

    @Transactional
    public LanguageVersionResponse update(UUID cvId, UUID languageVersionId, UpdateLanguageVersionRequest request) {
        CvLanguageVersion version = getOwnedInCv(cvId, languageVersionId);
        if (!version.getVersion().equals(request.version())) {
            throw new OptimisticLockException();
        }
        sectionOrderValidator.validate(request.sectionOrder());
        revisionService.captureLanguage(version);
        version.setName(request.name());
        version.setTemplateCode(request.templateCode());
        version.setContent(cloner.clone(request.content()));
        version.setSectionOrder(new ArrayList<>(request.sectionOrder()));
        return LanguageVersionMapper.toResponse(languageVersionRepository.saveAndFlush(version));
    }

    @Transactional
    public void delete(UUID cvId, UUID languageVersionId) {
        CvLanguageVersion version = getOwnedInCv(cvId, languageVersionId);
        revisionService.deleteAll(RevisionDocumentType.LANGUAGE_VERSION, version.getId());
        languageVersionRepository.delete(version);
    }

    private CvLanguageVersion getOwnedInCv(UUID cvId, UUID languageVersionId) {
        MasterCv cv = cvAccessService.getOwnedCv(cvId);
        CvLanguageVersion version = cvAccessService.getOwnedLanguageVersion(languageVersionId);
        if (!version.getMasterCvId().equals(cv.getId())) {
            throw new LanguageVersionNotFoundException();
        }
        return version;
    }

    @Transactional(readOnly = true)
    public List<RevisionResponse> listRevisions(UUID cvId, UUID languageVersionId) {
        CvLanguageVersion version = getOwnedInCv(cvId, languageVersionId);
        return revisionService.list(RevisionDocumentType.LANGUAGE_VERSION, version.getId());
    }

    @Transactional
    public LanguageVersionResponse restoreRevision(
            UUID cvId, UUID languageVersionId, UUID revisionId, RestoreRevisionRequest request) {
        CvLanguageVersion version = getOwnedInCv(cvId, languageVersionId);
        if (!version.getVersion().equals(request.version())) {
            throw new OptimisticLockException();
        }
        CvContentRevision revision = revisionService.load(
                RevisionDocumentType.LANGUAGE_VERSION, version.getId(), revisionId);
        revisionService.captureLanguage(version);
        version.setContent(cloner.clone(revision.getContent()));
        version.setSectionOrder(new ArrayList<>(revision.getSectionOrder()));
        return LanguageVersionMapper.toResponse(languageVersionRepository.saveAndFlush(version));
    }
}
