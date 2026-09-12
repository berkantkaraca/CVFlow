package com.cvflow.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cvflow.ai.model.AiSuggestionPayload;
import com.cvflow.common.exception.InvalidSuggestionStateException;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.Experience;
import com.cvflow.cv.model.Skill;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SuggestionApplierTest {
    private final SuggestionApplier applier = new SuggestionApplier();

    @Test
    void appliesSummary() {
        CvContent content = CvContent.empty();

        applier.apply(content, new AiSuggestionPayload(CvSection.SUMMARY, null, "summary", "old", "new"));

        assertEquals("new", content.getSummary());
    }

    @Test
    void replacesMatchingDescriptionBullet() {
        UUID id = UUID.randomUUID();
        CvContent content = CvContent.empty();
        Experience experience = new Experience();
        experience.setId(id);
        experience.setDescription(new ArrayList<>(List.of("old bullet", "other")));
        content.setExperiences(new ArrayList<>(List.of(experience)));

        applier.apply(content, new AiSuggestionPayload(CvSection.EXPERIENCE, id, "description", "old bullet", "new"));

        assertEquals(List.of("new", "other"), content.getExperiences().get(0).getDescription());
    }

    @Test
    void rejectsMissingEntity() {
        CvContent content = CvContent.empty();

        assertThrows(
                InvalidSuggestionStateException.class,
                () -> applier.apply(
                        content,
                        new AiSuggestionPayload(CvSection.SKILLS, UUID.randomUUID(), "name", "a", "b")));
    }

    @Test
    void appliesSkillName() {
        UUID id = UUID.randomUUID();
        CvContent content = CvContent.empty();
        Skill skill = new Skill();
        skill.setId(id);
        skill.setName("Java");
        content.setSkills(new ArrayList<>(List.of(skill)));

        applier.apply(content, new AiSuggestionPayload(CvSection.SKILLS, id, "name", "Java", "Java 21"));

        assertEquals("Java 21", content.getSkills().get(0).getName());
    }
}
