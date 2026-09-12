package com.cvflow.ai.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cvflow.common.exception.InvalidFieldException;
import com.cvflow.cv.model.CvSection;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CvFieldValidatorTest {
    private final CvFieldValidator validator = new CvFieldValidator();

    @Test
    void acceptsAllowedFieldCombinations() {
        assertDoesNotThrow(() -> validator.validate(CvSection.SUMMARY, null, "summary"));
        assertDoesNotThrow(() -> validator.validate(CvSection.EXPERIENCE, UUID.randomUUID(), "company"));
        assertDoesNotThrow(() -> validator.validate(CvSection.SKILLS, UUID.randomUUID(), "category"));
    }

    @Test
    void rejectsUnknownField() {
        assertThrows(
                InvalidFieldException.class,
                () -> validator.validate(CvSection.EXPERIENCE, UUID.randomUUID(), "salary"));
    }

    @Test
    void rejectsEntityIdForSummary() {
        assertThrows(
                InvalidFieldException.class, () -> validator.validate(CvSection.SUMMARY, UUID.randomUUID(), "summary"));
    }

    @Test
    void requiresEntityIdForListSections() {
        assertThrows(InvalidFieldException.class, () -> validator.validate(CvSection.PROJECTS, null, "name"));
    }
}
