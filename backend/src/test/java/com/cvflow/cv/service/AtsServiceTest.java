package com.cvflow.cv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.cvflow.common.security.CvAccessService;
import com.cvflow.cv.dto.response.AtsScoreResponse;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.Experience;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.PersonalInfo;
import com.cvflow.cv.model.Skill;
import com.cvflow.cv.model.TemplateCode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AtsServiceTest {

    @Mock
    private CvAccessService cvAccessService;

    private AtsService service;
    private final UUID cvId = UUID.randomUUID();
    private final UUID versionId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        service = new AtsService(cvAccessService);
    }

    @Test
    void fullContentScoresHigh() {
        CvContent content = richContent();
        when(cvAccessService.getOwnedLanguageVersion(versionId)).thenReturn(version(content));
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(masterCv());

        AtsScoreResponse response = service.score(cvId, versionId);

        assertTrue(response.score() >= 90);
        assertEquals(12, response.checks().size());
    }

    @Test
    void emptyContentScoresZero() {
        when(cvAccessService.getOwnedLanguageVersion(versionId)).thenReturn(version(CvContent.empty()));
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(masterCv());

        AtsScoreResponse response = service.score(cvId, versionId);

        assertTrue(response.score() < 20);
    }

    private CvContent richContent() {
        CvContent content = CvContent.empty();
        PersonalInfo personal = new PersonalInfo();
        personal.setEmail("jane@example.com");
        personal.setPhone("+1");
        content.setPersonal(personal);
        content.setSummary("Experienced backend developer with a focus on clean architectures and APIs.");
        Experience experience = new Experience();
        experience.setId(UUID.randomUUID());
        experience.setCompany("Acme");
        experience.setPosition("Developer");
        experience.setStartDate("2021-01");
        experience.setDescription(
                new ArrayList<>(List.of("Built APIs", "Improved latency", "Mentored juniors")));
        content.setExperiences(new ArrayList<>(List.of(experience)));
        for (String skill : List.of("Java", "Spring", "SQL")) {
            Skill s = new Skill();
            s.setId(UUID.randomUUID());
            s.setName(skill);
            content.getSkills().add(s);
        }
        com.cvflow.cv.model.Education education = new com.cvflow.cv.model.Education();
        education.setId(UUID.randomUUID());
        education.setInstitution("University");
        content.setEducation(new ArrayList<>(List.of(education)));
        com.cvflow.cv.model.Language language = new com.cvflow.cv.model.Language();
        language.setId(UUID.randomUUID());
        language.setName("English");
        content.setLanguages(new ArrayList<>(List.of(language)));
        com.cvflow.cv.model.Link link = new com.cvflow.cv.model.Link();
        link.setId(UUID.randomUUID());
        link.setLabel("GitHub");
        content.setLinks(new ArrayList<>(List.of(link)));
        return content;
    }

    private CvLanguageVersion version(CvContent content) {
        CvLanguageVersion version = new CvLanguageVersion();
        version.setId(versionId);
        version.setMasterCvId(cvId);
        version.setLanguageCode(LanguageCode.EN);
        version.setName("English");
        version.setTemplateCode(TemplateCode.MODERN);
        version.setContent(content);
        version.setSectionOrder(CvSection.defaultOrder());
        version.setVersion(0L);
        return version;
    }

    private MasterCv masterCv() {
        MasterCv cv = new MasterCv();
        cv.setId(cvId);
        cv.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        cv.setName("CV");
        cv.setContent(CvContent.empty());
        cv.setSectionOrder(CvSection.defaultOrder());
        cv.setVersion(0L);
        return cv;
    }
}
