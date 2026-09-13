package com.cvflow.cv.service;

import com.cvflow.common.exception.OptimisticLockException;
import com.cvflow.common.security.CvAccessService;
import com.cvflow.common.security.CurrentUserService;
import com.cvflow.cv.dto.request.CreateCvRequest;
import com.cvflow.cv.dto.request.RestoreRevisionRequest;
import com.cvflow.cv.dto.request.UpdateCvRequest;
import com.cvflow.cv.dto.response.CvResponse;
import com.cvflow.cv.dto.response.CvSummaryResponse;
import com.cvflow.cv.dto.response.RevisionResponse;
import com.cvflow.cv.entity.CvContentRevision;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.mapper.CvMapper;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.RevisionDocumentType;
import com.cvflow.cv.repository.CvLanguageVersionRepository;
import com.cvflow.cv.repository.MasterCvRepository;
import com.cvflow.billing.service.UsageService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CvService {
    private final MasterCvRepository masterCvRepository;
    private final CvLanguageVersionRepository languageVersionRepository;
    private final CurrentUserService currentUserService;
    private final CvAccessService cvAccessService;
    private final CvContentCloner cloner;
    private final SectionOrderValidator sectionOrderValidator;
    private final RevisionService revisionService;
    private final UsageService usageService;

    @Transactional(readOnly = true)
    public List<CvSummaryResponse> list() {
        return masterCvRepository.findAllByUserIdOrderByUpdatedAtDesc(currentUserService.getCurrentUserId())
                .stream()
                .map(CvMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public CvResponse get(UUID cvId) {
        return CvMapper.toResponse(cvAccessService.getOwnedCv(cvId));
    }

    @Transactional
    public CvResponse create(CreateCvRequest request) {
        UUID userId = currentUserService.getCurrentUserId();
        usageService.checkCvQuota(userId);
        MasterCv cv = new MasterCv();
        cv.setUserId(userId);
        cv.setName(request.name());
        cv.setContent(CvContent.empty());
        cv.setSectionOrder(CvSection.defaultOrder());
        return CvMapper.toResponse(masterCvRepository.saveAndFlush(cv));
    }

    @Transactional
    public CvResponse update(UUID cvId, UpdateCvRequest request) {
        MasterCv cv = cvAccessService.getOwnedCv(cvId);
        if (!cv.getVersion().equals(request.version())) {
            throw new OptimisticLockException();
        }
        sectionOrderValidator.validate(request.sectionOrder());
        revisionService.captureMaster(cv);
        cv.setName(request.name());
        cv.setContent(cloner.clone(request.content()));
        cv.setSectionOrder(new ArrayList<>(request.sectionOrder()));
        return CvMapper.toResponse(masterCvRepository.saveAndFlush(cv));
    }

    @Transactional
    public void delete(UUID cvId) {
        MasterCv cv = cvAccessService.getOwnedCv(cvId);
        revisionService.deleteAll(RevisionDocumentType.MASTER_CV, cvId);
        masterCvRepository.delete(cv);
    }

    @Transactional
    public CvResponse duplicate(UUID cvId) {
        MasterCv source = cvAccessService.getOwnedCv(cvId);
        MasterCv copy = new MasterCv();
        copy.setUserId(source.getUserId());
        copy.setName(source.getName() + " (Copy)");
        copy.setContent(cloner.clone(source.getContent()));
        copy.setSectionOrder(new ArrayList<>(source.getSectionOrder()));
        MasterCv saved = masterCvRepository.saveAndFlush(copy);
        for (CvLanguageVersion languageVersion : languageVersionRepository.findAllByMasterCvId(source.getId())) {
            CvLanguageVersion languageCopy = new CvLanguageVersion();
            languageCopy.setMasterCvId(saved.getId());
            languageCopy.setLanguageCode(languageVersion.getLanguageCode());
            languageCopy.setName(languageVersion.getName());
            languageCopy.setTemplateCode(languageVersion.getTemplateCode());
            languageCopy.setContent(cloner.clone(languageVersion.getContent()));
            languageCopy.setSectionOrder(new ArrayList<>(languageVersion.getSectionOrder()));
            languageVersionRepository.save(languageCopy);
        }
        return CvMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RevisionResponse> listRevisions(UUID cvId) {
        MasterCv cv = cvAccessService.getOwnedCv(cvId);
        return revisionService.list(RevisionDocumentType.MASTER_CV, cv.getId());
    }

    @Transactional
    public CvResponse restoreRevision(UUID cvId, UUID revisionId, RestoreRevisionRequest request) {
        MasterCv cv = cvAccessService.getOwnedCv(cvId);
        if (!cv.getVersion().equals(request.version())) {
            throw new OptimisticLockException();
        }
        CvContentRevision revision =
                revisionService.load(RevisionDocumentType.MASTER_CV, cv.getId(), revisionId);
        revisionService.captureMaster(cv);
        cv.setContent(cloner.clone(revision.getContent()));
        cv.setSectionOrder(new ArrayList<>(revision.getSectionOrder()));
        return CvMapper.toResponse(masterCvRepository.saveAndFlush(cv));
    }
}
