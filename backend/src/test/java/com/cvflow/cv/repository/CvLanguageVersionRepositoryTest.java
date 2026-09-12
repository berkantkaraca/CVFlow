package com.cvflow.cv.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class CvLanguageVersionRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer("postgres:17");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private CvLanguageVersionRepository languageVersionRepository;

    @Autowired
    private MasterCvRepository masterCvRepository;

    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void persistsAndFindsByMasterAndLanguage() {
        MasterCv cv = savedCv("Repo CV");
        savedVersion(cv.getId(), LanguageCode.EN);

        var found = languageVersionRepository.findByMasterCvIdAndLanguageCode(cv.getId(), LanguageCode.EN);

        assertTrue(found.isPresent());
        assertEquals(TemplateCode.MODERN, found.get().getTemplateCode());
        assertEquals(CvSection.defaultOrder(), found.get().getSectionOrder());
    }

    @Test
    void uniqueConstraintPreventsDuplicateLanguage() {
        MasterCv cv = savedCv("Unique CV");
        savedVersion(cv.getId(), LanguageCode.TR);

        assertThrows(DataIntegrityViolationException.class, () -> savedVersion(cv.getId(), LanguageCode.TR));
    }

    @Test
    void findsAllByMasterCvId() {
        MasterCv cv = savedCv("List CV");
        savedVersion(cv.getId(), LanguageCode.EN);
        savedVersion(cv.getId(), LanguageCode.TR);

        assertEquals(2, languageVersionRepository.findAllByMasterCvId(cv.getId()).size());
    }

    private MasterCv savedCv(String name) {
        MasterCv cv = new MasterCv();
        cv.setUserId(userId);
        cv.setName(name);
        cv.setContent(CvContent.empty());
        cv.setSectionOrder(CvSection.defaultOrder());
        return masterCvRepository.save(cv);
    }

    private CvLanguageVersion savedVersion(UUID masterCvId, LanguageCode languageCode) {
        CvLanguageVersion version = new CvLanguageVersion();
        version.setMasterCvId(masterCvId);
        version.setLanguageCode(languageCode);
        version.setName(languageCode.name());
        version.setTemplateCode(TemplateCode.MODERN);
        version.setContent(CvContent.empty());
        version.setSectionOrder(new ArrayList<>(CvSection.defaultOrder()));
        return languageVersionRepository.saveAndFlush(version);
    }
}
