package com.cvflow.cv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.Experience;
import tools.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CvContentClonerTest {

    private final CvContentCloner cloner = new CvContentCloner(new ObjectMapper());

    @Test
    void cloneCreatesDeepCopy() {
        CvContent source = CvContent.empty();
        source.setSummary("Backend developer");
        Experience exp = new Experience();
        exp.setId(UUID.randomUUID());
        exp.setCompany("Acme");
        exp.setDescription(new ArrayList<>(List.of("Did things")));
        source.setExperiences(new ArrayList<>(List.of(exp)));

        CvContent copy = cloner.clone(source);

        assertNotSame(source, copy);
        assertEquals("Backend developer", copy.getSummary());
        assertEquals(1, copy.getExperiences().size());

        copy.getExperiences().get(0).getDescription().add("More things");
        assertEquals(1, source.getExperiences().get(0).getDescription().size());
    }

    @Test
    void cloneNullReturnsEmpty() {
        CvContent copy = cloner.clone(null);

        assertNotNull(copy);
        assertNotNull(copy.getExperiences());
    }
}
