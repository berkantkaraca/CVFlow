package com.cvflow.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.Experience;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CvContentDifferTest {
    private final CvContentDiffer differ = new CvContentDiffer();

    @Test
    void detectsChangedScalarFields() {
        CvContent target = CvContent.empty();
        target.setSummary("old");
        CvContent translated = CvContent.empty();
        translated.setSummary("new");

        var diffs = differ.diff(translated, target);

        assertEquals(1, diffs.size());
        assertEquals(new CvContentDiffer.TranslationDiff(CvSection.SUMMARY, null, "summary", "old", "new"), diffs.get(0));
    }

    @Test
    void skipsEqualAndBlankValues() {
        CvContent target = CvContent.empty();
        target.setSummary("same");
        CvContent translated = CvContent.empty();
        translated.setSummary("same");

        assertTrue(differ.diff(translated, target).isEmpty());

        translated.setSummary("   ");
        assertTrue(differ.diff(translated, target).isEmpty());
    }

    @Test
    void matchesListEntriesByIdAndSkipsUnknown() {
        UUID id = UUID.randomUUID();
        CvContent target = CvContent.empty();
        Experience current = new Experience();
        current.setId(id);
        current.setCompany("Acme");
        current.setDescription(new ArrayList<>(List.of("a", "b")));
        target.setExperiences(new ArrayList<>(List.of(current)));
        CvContent translated = CvContent.empty();
        Experience next = new Experience();
        next.setId(id);
        next.setCompany("Acme");
        next.setDescription(new ArrayList<>(List.of("a2", "b")));
        translated.setExperiences(new ArrayList<>(List.of(next)));
        Experience orphan = new Experience();
        orphan.setId(UUID.randomUUID());
        orphan.setCompany("Other");
        translated.getExperiences().add(orphan);

        var diffs = differ.diff(translated, target);

        assertEquals(1, diffs.size());
        assertEquals("description", diffs.get(0).field());
        assertEquals("a", diffs.get(0).original());
        assertEquals("a2", diffs.get(0).suggested());
    }
}
