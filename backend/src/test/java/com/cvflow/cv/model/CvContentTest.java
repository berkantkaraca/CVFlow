package com.cvflow.cv.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cvflow.cv.service.CvContentCloner;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class CvContentTest {

    @Test
    void emptyCreatesValidStructure() {
        CvContent content = CvContent.empty();

        assertNotNull(content);
        assertNotNull(content.getPersonal());
        assertNotNull(content.getExperiences());
        assertNotNull(content.getEducation());
        assertNotNull(content.getProjects());
        assertNotNull(content.getSkills());
        assertNotNull(content.getLanguages());
        assertNotNull(content.getCertifications());
        assertNotNull(content.getLinks());
    }

    @Test
    void jsonRoundTripPreservesEquality() {
        CvContentCloner cloner = new CvContentCloner(new ObjectMapper());
        CvContent source = CvContent.empty();
        source.setSummary("Backend developer");

        assertEquals(source, cloner.clone(source));
        assertEquals(source.hashCode(), cloner.clone(source).hashCode());
    }

    @Test
    void defaultOrderContainsAllSectionsOnce() {        List<CvSection> order = CvSection.defaultOrder();

        assertEquals(8, order.size());
        assertEquals(CvSection.values().length, order.stream().distinct().count());
        assertTrue(order.contains(CvSection.SUMMARY));
        assertEquals(CvSection.SUMMARY, order.get(0));
    }
}
