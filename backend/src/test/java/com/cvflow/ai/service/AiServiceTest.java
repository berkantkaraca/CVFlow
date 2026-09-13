package com.cvflow.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cvflow.ai.client.AiClient;
import com.cvflow.ai.dto.AiSuggestionResponse;
import com.cvflow.ai.dto.ImproveRequest;
import com.cvflow.ai.dto.TranslateRequest;
import com.cvflow.ai.entity.CvAiSuggestion;
import com.cvflow.ai.model.AiOperation;
import com.cvflow.ai.model.AiSuggestionPayload;
import com.cvflow.ai.model.AiSuggestionStatus;
import com.cvflow.ai.repository.CvAiSuggestionRepository;
import com.cvflow.common.exception.AccessDeniedException;
import com.cvflow.common.exception.InvalidFieldException;
import com.cvflow.common.exception.InvalidSuggestionStateException;
import com.cvflow.common.exception.SuggestionNotFoundException;
import com.cvflow.common.security.CvAccessService;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.Experience;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import com.cvflow.cv.repository.CvLanguageVersionRepository;
import com.cvflow.cv.service.RevisionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private AiClient aiClient;

    @Mock
    private CvAiSuggestionRepository suggestionRepository;

    @Mock
    private CvLanguageVersionRepository languageVersionRepository;

    @Mock
    private CvAccessService cvAccessService;

    @Mock
    private RevisionService revisionService;

    @Mock
    private com.cvflow.billing.service.UsageService usageService;

    private AiService service;
    private final UUID versionId = UUID.randomUUID();
    private final UUID suggestionId = UUID.randomUUID();
    private final UUID experienceId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        service = new AiService(
                aiClient,
                suggestionRepository,
                languageVersionRepository,
                cvAccessService,
                new CvFieldValidator(),
                new SuggestionApplier(),
                new CvContentDiffer(),
                revisionService,
                usageService);
        org.mockito.Mockito.lenient()
                .when(cvAccessService.getOwnedCv(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(i -> masterCv(UUID.randomUUID()));
    }

    @Test
    void improveCreatesPendingSuggestionWithoutMutatingContent() {
        CvLanguageVersion version = languageVersion();
        when(cvAccessService.getOwnedLanguageVersion(versionId)).thenReturn(version);
        when(aiClient.improve("Did stuff")).thenReturn("Did great stuff");
        when(suggestionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AiSuggestionResponse response = service.improve(improveRequest());

        assertEquals(AiOperation.IMPROVE, response.operation());
        assertEquals(AiSuggestionStatus.PENDING, response.status());
        assertEquals("Did great stuff", response.payload().getSuggested());
        assertEquals("Did stuff", response.payload().getOriginal());
        verify(languageVersionRepository, never()).saveAndFlush(any());
    }

    @Test
    void improveRejectsDisallowedField() {
        when(cvAccessService.getOwnedLanguageVersion(versionId)).thenReturn(languageVersion());

        ImproveRequest request =
                new ImproveRequest(versionId, CvSection.EXPERIENCE, experienceId, "salary", "x");

        assertThrows(InvalidFieldException.class, () -> service.improve(request));
    }

    @Test
    void improvePropagatesAccessDenied() {
        when(cvAccessService.getOwnedLanguageVersion(versionId)).thenThrow(new AccessDeniedException());

        assertThrows(AccessDeniedException.class, () -> service.improve(improveRequest()));
    }

    @Test
    void acceptAppliesSuggestionAndMarksAccepted() {
        CvLanguageVersion version = languageVersion();
        CvAiSuggestion suggestion = suggestion(AiSuggestionStatus.PENDING);
        when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(suggestion));
        when(cvAccessService.getOwnedLanguageVersion(versionId)).thenReturn(version);
        when(languageVersionRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(suggestionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AiSuggestionResponse response = service.accept(suggestionId);

        assertEquals(AiSuggestionStatus.ACCEPTED, response.status());
        assertEquals(List.of("Did great stuff"), version.getContent().getExperiences().get(0).getDescription());
    }

    @Test
    void acceptRejectsNonPending() {
        when(suggestionRepository.findById(suggestionId))
                .thenReturn(Optional.of(suggestion(AiSuggestionStatus.ACCEPTED)));
        when(cvAccessService.getOwnedLanguageVersion(versionId)).thenReturn(languageVersion());

        assertThrows(InvalidSuggestionStateException.class, () -> service.accept(suggestionId));
    }

    @Test
    void rejectMarksRejectedWithoutTouchingContent() {
        CvAiSuggestion suggestion = suggestion(AiSuggestionStatus.PENDING);
        when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(suggestion));
        when(cvAccessService.getOwnedLanguageVersion(versionId)).thenReturn(languageVersion());
        when(suggestionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AiSuggestionResponse response = service.reject(suggestionId);

        assertEquals(AiSuggestionStatus.REJECTED, response.status());
        verify(languageVersionRepository, never()).saveAndFlush(any());
    }

    @Test
    void acceptThrowsWhenMissing() {
        when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.empty());

        assertThrows(SuggestionNotFoundException.class, () -> service.accept(suggestionId));
    }

    @Test
    void tailorCreatesTailorSuggestions() {
        CvLanguageVersion version = languageVersion();
        when(cvAccessService.getOwnedLanguageVersion(versionId)).thenReturn(version);
        when(cvAccessService.getOwnedCv(version.getMasterCvId())).thenReturn(masterCv(version.getMasterCvId()));
        CvContent tailored = CvContent.empty();
        tailored.setSummary("Tailored summary");
        when(aiClient.tailorContent(any(), any(), any())).thenReturn(tailored);
        when(suggestionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var suggestions = service.tailor(new com.cvflow.ai.dto.TailorRequest(versionId, "Hiring Java devs"));

        assertEquals(1, suggestions.size());
        assertEquals(AiOperation.TAILOR, suggestions.get(0).operation());
    }

    @Test
    void generateCoverLetterReturnsDraftWithoutPersisting() {
        MasterCv master = masterCv(UUID.randomUUID());
        when(cvAccessService.getOwnedCv(master.getId())).thenReturn(master);
        when(aiClient.generateCoverLetter(any(), any(), any())).thenReturn("Dear team,");

        var draft = service.generateCoverLetter(
                new com.cvflow.ai.dto.CoverLetterRequest(master.getId(), LanguageCode.EN, "JD"));

        assertEquals("Dear team,", draft.body());
        verify(suggestionRepository, org.mockito.Mockito.never()).save(any());
    }

    private MasterCv masterCv(UUID id) {
        MasterCv cv = new MasterCv();
        cv.setId(id);
        cv.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        cv.setName("CV");
        cv.setContent(CvContent.empty());
        cv.setSectionOrder(CvSection.defaultOrder());
        cv.setVersion(0L);
        return cv;
    }

    @Test
    void translateCreatesOneSuggestionPerChangedField() {
        UUID sourceId = UUID.randomUUID();
        CvLanguageVersion source = languageVersion();
        source.setId(sourceId);
        source.setLanguageCode(LanguageCode.TR);
        CvLanguageVersion target = languageVersion();
        when(cvAccessService.getOwnedLanguageVersion(sourceId)).thenReturn(source);
        when(cvAccessService.getOwnedLanguageVersion(versionId)).thenReturn(target);
        CvContent translated = CvContent.empty();
        translated.setSummary("Kıdemli geliştirici");
        when(aiClient.translateContent(any(), any(), any())).thenReturn(translated);
        when(suggestionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var suggestions = service.translate(new TranslateRequest(sourceId, versionId));

        assertEquals(1, suggestions.size());
        assertEquals(AiOperation.TRANSLATE, suggestions.get(0).operation());
        assertEquals(CvSection.SUMMARY, suggestions.get(0).payload().getSection());
        assertEquals("Kıdemli geliştirici", suggestions.get(0).payload().getSuggested());
    }

    @Test
    void translateRejectsSameLanguage() {
        CvLanguageVersion source = languageVersion();
        source.setLanguageCode(LanguageCode.EN);
        when(cvAccessService.getOwnedLanguageVersion(source.getId())).thenReturn(source);
        when(cvAccessService.getOwnedLanguageVersion(versionId)).thenReturn(languageVersion());

        assertThrows(
                com.cvflow.common.exception.InvalidFieldException.class,
                () -> service.translate(new TranslateRequest(source.getId(), versionId)));
    }

    @Test
    void acceptAllAppliesEveryPendingSuggestion() {
        CvLanguageVersion version = languageVersion();
        version.getContent().setSummary("old summary");
        CvAiSuggestion first = suggestion(AiSuggestionStatus.PENDING);
        first.getPayload().setSection(CvSection.SUMMARY);
        first.getPayload().setEntityId(null);
        first.getPayload().setField("summary");
        first.getPayload().setOriginal("old summary");
        first.getPayload().setSuggested("new summary");
        CvAiSuggestion second = suggestion(AiSuggestionStatus.PENDING);
        when(cvAccessService.getOwnedLanguageVersion(versionId)).thenReturn(version);
        when(suggestionRepository.findAllByLanguageVersionIdAndStatus(versionId, AiSuggestionStatus.PENDING))
                .thenReturn(new ArrayList<>(List.of(first, second)));
        when(languageVersionRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(suggestionRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        var accepted = service.acceptAll(new com.cvflow.ai.dto.AcceptAllRequest(versionId));

        assertEquals(2, accepted.size());
        assertEquals("new summary", version.getContent().getSummary());
        verify(languageVersionRepository).saveAndFlush(any());
    }

    private ImproveRequest improveRequest() {
        return new ImproveRequest(versionId, CvSection.EXPERIENCE, experienceId, "description", "Did stuff");
    }

    private CvLanguageVersion languageVersion() {
        CvContent content = CvContent.empty();
        Experience experience = new Experience();
        experience.setId(experienceId);
        experience.setCompany("Acme");
        experience.setDescription(new ArrayList<>(List.of("Did stuff")));
        content.setExperiences(new ArrayList<>(List.of(experience)));
        CvLanguageVersion version = new CvLanguageVersion();
        version.setId(versionId);
        version.setMasterCvId(UUID.randomUUID());
        version.setLanguageCode(LanguageCode.EN);
        version.setName("English");
        version.setTemplateCode(TemplateCode.MODERN);
        version.setContent(content);
        version.setSectionOrder(CvSection.defaultOrder());
        version.setVersion(0L);
        return version;
    }

    private CvAiSuggestion suggestion(AiSuggestionStatus status) {
        CvAiSuggestion suggestion = new CvAiSuggestion();
        suggestion.setId(suggestionId);
        suggestion.setLanguageVersionId(versionId);
        suggestion.setOperation(AiOperation.IMPROVE);
        suggestion.setStatus(status);
        suggestion.setPayload(new AiSuggestionPayload(
                CvSection.EXPERIENCE, experienceId, "description", "Did stuff", "Did great stuff"));
        return suggestion;
    }
}
