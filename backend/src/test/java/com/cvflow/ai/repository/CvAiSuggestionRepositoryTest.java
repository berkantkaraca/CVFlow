package com.cvflow.ai.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cvflow.ai.entity.CvAiSuggestion;
import com.cvflow.ai.model.AiOperation;
import com.cvflow.ai.model.AiSuggestionPayload;
import com.cvflow.ai.model.AiSuggestionStatus;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class CvAiSuggestionRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer("postgres:17");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private CvAiSuggestionRepository repository;

    @Autowired
    private com.cvflow.cv.repository.CvLanguageVersionRepository languageVersionRepository;

    @Autowired
    private com.cvflow.cv.repository.MasterCvRepository masterCvRepository;

    @Test
    void persistsPayloadAndFindsByStatus() {
        UUID versionId = savedLanguageVersion().getId();
        repository.save(suggestion(versionId, AiSuggestionStatus.PENDING));
        repository.save(suggestion(versionId, AiSuggestionStatus.ACCEPTED));

        assertEquals(2, repository.findAllByLanguageVersionId(versionId).size());
        assertEquals(
                1,
                repository
                        .findAllByLanguageVersionIdAndStatus(versionId, AiSuggestionStatus.PENDING)
                        .size());
    }

    private CvLanguageVersion savedLanguageVersion() {
        MasterCv cv = new MasterCv();
        cv.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        cv.setName("AI CV");
        cv.setContent(CvContent.empty());
        cv.setSectionOrder(CvSection.defaultOrder());
        MasterCv savedCv = masterCvRepository.save(cv);
        CvLanguageVersion version = new CvLanguageVersion();
        version.setMasterCvId(savedCv.getId());
        version.setLanguageCode(LanguageCode.EN);
        version.setName("English");
        version.setTemplateCode(TemplateCode.MODERN);
        version.setContent(CvContent.empty());
        version.setSectionOrder(new ArrayList<>(CvSection.defaultOrder()));
        return languageVersionRepository.save(version);
    }

    private CvAiSuggestion suggestion(UUID versionId, AiSuggestionStatus status) {
        CvAiSuggestion suggestion = new CvAiSuggestion();
        suggestion.setLanguageVersionId(versionId);
        suggestion.setOperation(AiOperation.IMPROVE);
        suggestion.setStatus(status);
        suggestion.setPayload(new AiSuggestionPayload(CvSection.SUMMARY, null, "summary", "old", "new"));
        return suggestion;
    }
}
