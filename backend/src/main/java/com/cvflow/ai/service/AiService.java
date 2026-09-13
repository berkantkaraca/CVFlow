package com.cvflow.ai.service;

import com.cvflow.ai.client.AiClient;
import com.cvflow.ai.dto.AcceptAllRequest;
import com.cvflow.ai.dto.AiSuggestionResponse;
import com.cvflow.ai.dto.CoverLetterDraftResponse;
import com.cvflow.ai.dto.CoverLetterRequest;
import com.cvflow.ai.dto.ImproveRequest;
import com.cvflow.ai.dto.TailorRequest;
import com.cvflow.ai.dto.TranslateRequest;
import com.cvflow.ai.entity.CvAiSuggestion;
import com.cvflow.ai.mapper.AiSuggestionMapper;
import com.cvflow.ai.model.AiOperation;
import com.cvflow.ai.model.AiSuggestionPayload;
import com.cvflow.ai.model.AiSuggestionStatus;
import com.cvflow.ai.repository.CvAiSuggestionRepository;
import com.cvflow.common.exception.InvalidFieldException;
import com.cvflow.common.exception.InvalidSuggestionStateException;
import com.cvflow.common.exception.SuggestionNotFoundException;
import com.cvflow.common.security.CvAccessService;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.repository.CvLanguageVersionRepository;
import com.cvflow.cv.service.RevisionService;
import com.cvflow.billing.service.UsageService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiService {
    private final AiClient aiClient;
    private final CvAiSuggestionRepository suggestionRepository;
    private final CvLanguageVersionRepository languageVersionRepository;
    private final CvAccessService cvAccessService;
    private final CvFieldValidator fieldValidator;
    private final SuggestionApplier applier;
    private final CvContentDiffer differ;
    private final RevisionService revisionService;
    private final UsageService usageService;

    @Transactional
    public AiSuggestionResponse improve(ImproveRequest request) {
        CvLanguageVersion version = cvAccessService.getOwnedLanguageVersion(request.languageVersionId());
        fieldValidator.validate(request.section(), request.entityId(), request.field());
        UUID userId = ownerOf(version);
        usageService.checkAiQuota(userId);
        String suggested = aiClient.improve(request.content());
        usageService.recordAiUsage(userId, AiOperation.IMPROVE);
        return saveSuggestion(version.getId(), AiOperation.IMPROVE, request, suggested);
    }

    @Transactional
    public AiSuggestionResponse rewrite(ImproveRequest request) {
        CvLanguageVersion version = cvAccessService.getOwnedLanguageVersion(request.languageVersionId());
        fieldValidator.validate(request.section(), request.entityId(), request.field());
        UUID userId = ownerOf(version);
        usageService.checkAiQuota(userId);
        String suggested = aiClient.rewrite(request.content());
        usageService.recordAiUsage(userId, AiOperation.REWRITE);
        return saveSuggestion(version.getId(), AiOperation.REWRITE, request, suggested);
    }

    @Transactional
    public List<AiSuggestionResponse> translate(TranslateRequest request) {
        CvLanguageVersion source = cvAccessService.getOwnedLanguageVersion(request.sourceLanguageVersionId());
        CvLanguageVersion target = cvAccessService.getOwnedLanguageVersion(request.targetLanguageVersionId());
        if (source.getLanguageCode() == target.getLanguageCode()) {
            throw new InvalidFieldException("Source and target language versions must have different languages");
        }
        UUID userId = ownerOf(target);
        usageService.checkAiQuota(userId);
        com.cvflow.cv.model.CvContent translated = aiClient.translateContent(
                source.getContent(), source.getLanguageCode(), target.getLanguageCode());
        usageService.recordAiUsage(userId, AiOperation.TRANSLATE);
        List<CvAiSuggestion> suggestions = differ.diff(translated, target.getContent()).stream()
                .map(diff -> {
                    CvAiSuggestion suggestion = new CvAiSuggestion();
                    suggestion.setLanguageVersionId(target.getId());
                    suggestion.setOperation(AiOperation.TRANSLATE);
                    suggestion.setStatus(AiSuggestionStatus.PENDING);
                    suggestion.setPayload(new AiSuggestionPayload(
                            diff.section(), diff.entityId(), diff.field(), diff.original(), diff.suggested()));
                    return suggestion;
                })
                .toList();
        return suggestionRepository.saveAll(suggestions).stream()
                .map(AiSuggestionMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<AiSuggestionResponse> tailor(TailorRequest request) {
        CvLanguageVersion version = cvAccessService.getOwnedLanguageVersion(request.languageVersionId());
        UUID userId = ownerOf(version);
        usageService.checkAiQuota(userId);
        com.cvflow.cv.model.CvContent tailored =
                aiClient.tailorContent(version.getContent(), request.jobDescription(), version.getLanguageCode());
        usageService.recordAiUsage(userId, AiOperation.TAILOR);
        List<CvAiSuggestion> suggestions = differ.diff(tailored, version.getContent()).stream()
                .map(diff -> {
                    CvAiSuggestion suggestion = new CvAiSuggestion();
                    suggestion.setLanguageVersionId(version.getId());
                    suggestion.setOperation(AiOperation.TAILOR);
                    suggestion.setStatus(AiSuggestionStatus.PENDING);
                    suggestion.setPayload(new AiSuggestionPayload(
                            diff.section(), diff.entityId(), diff.field(), diff.original(), diff.suggested()));
                    return suggestion;
                })
                .toList();
        return suggestionRepository.saveAll(suggestions).stream()
                .map(AiSuggestionMapper::toResponse)
                .toList();
    }

    @Transactional
    public CoverLetterDraftResponse generateCoverLetter(CoverLetterRequest request) {
        MasterCv master = cvAccessService.getOwnedCv(request.masterCvId());
        UUID userId = master.getUserId();
        usageService.checkAiQuota(userId);
        String body = aiClient.generateCoverLetter(
                master.getContent(), request.jobDescription(), request.languageCode());
        usageService.recordAiUsage(userId, AiOperation.COVER_LETTER);
        return new CoverLetterDraftResponse(body);
    }

    private UUID ownerOf(CvLanguageVersion version) {
        MasterCv master = cvAccessService.getOwnedCv(version.getMasterCvId());
        return master.getUserId();
    }

    @Transactional
    public List<AiSuggestionResponse> acceptAll(AcceptAllRequest request) {
        CvLanguageVersion version = cvAccessService.getOwnedLanguageVersion(request.languageVersionId());
        List<CvAiSuggestion> pending =
                suggestionRepository.findAllByLanguageVersionIdAndStatus(version.getId(), AiSuggestionStatus.PENDING);
        if (!pending.isEmpty()) {
            revisionService.captureLanguage(version);
        }
        for (CvAiSuggestion suggestion : pending) {
            applier.apply(version.getContent(), suggestion.getPayload());
            suggestion.setStatus(AiSuggestionStatus.ACCEPTED);
        }
        languageVersionRepository.saveAndFlush(version);
        List<CvAiSuggestion> saved = suggestionRepository.saveAll(pending);
        return saved.stream().map(AiSuggestionMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AiSuggestionResponse> list(UUID languageVersionId, AiSuggestionStatus status) {
        CvLanguageVersion version = cvAccessService.getOwnedLanguageVersion(languageVersionId);
        List<CvAiSuggestion> suggestions = status == null
                ? suggestionRepository.findAllByLanguageVersionId(version.getId())
                : suggestionRepository.findAllByLanguageVersionIdAndStatus(version.getId(), status);
        return suggestions.stream().map(AiSuggestionMapper::toResponse).toList();
    }

    @Transactional
    public AiSuggestionResponse accept(UUID suggestionId) {
        CvAiSuggestion suggestion = loadOwned(suggestionId);
        requirePending(suggestion);
        CvLanguageVersion version = cvAccessService.getOwnedLanguageVersion(suggestion.getLanguageVersionId());
        revisionService.captureLanguage(version);
        applier.apply(version.getContent(), suggestion.getPayload());
        languageVersionRepository.saveAndFlush(version);
        suggestion.setStatus(AiSuggestionStatus.ACCEPTED);
        return AiSuggestionMapper.toResponse(suggestionRepository.save(suggestion));
    }

    @Transactional
    public AiSuggestionResponse reject(UUID suggestionId) {
        CvAiSuggestion suggestion = loadOwned(suggestionId);
        requirePending(suggestion);
        suggestion.setStatus(AiSuggestionStatus.REJECTED);
        return AiSuggestionMapper.toResponse(suggestionRepository.save(suggestion));
    }

    private CvAiSuggestion loadOwned(UUID suggestionId) {
        CvAiSuggestion suggestion =
                suggestionRepository.findById(suggestionId).orElseThrow(SuggestionNotFoundException::new);
        cvAccessService.getOwnedLanguageVersion(suggestion.getLanguageVersionId());
        return suggestion;
    }

    private void requirePending(CvAiSuggestion suggestion) {
        if (suggestion.getStatus() != AiSuggestionStatus.PENDING) {
            throw new InvalidSuggestionStateException();
        }
    }

    private AiSuggestionResponse saveSuggestion(
            UUID languageVersionId, AiOperation operation, ImproveRequest request, String suggested) {
        CvAiSuggestion suggestion = new CvAiSuggestion();
        suggestion.setLanguageVersionId(languageVersionId);
        suggestion.setOperation(operation);
        suggestion.setStatus(AiSuggestionStatus.PENDING);
        suggestion.setPayload(new AiSuggestionPayload(
                request.section(), request.entityId(), request.field(), request.content(), suggested));
        return AiSuggestionMapper.toResponse(suggestionRepository.save(suggestion));
    }
}
