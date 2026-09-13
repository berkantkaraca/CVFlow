package com.cvflow.ai.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class AiOutputValidatorTest {
    private final AiOutputValidator validator = new AiOutputValidator();

    @Test
    void acceptsFaithfulRewording() {
        assertEquals(
                Optional.empty(),
                validator.violation(
                        "Developed backend services with Java in 2021.",
                        "Engineered backend services using Java in 2021."));
    }

    @Test
    void rejectsInventedPercentageAndCounts() {
        var violation = validator.violation(
                "Developed backend services with Java.",
                "Developed backend services with Java, improving latency by 30% for 100K users.");

        assertTrue(violation.isPresent());
        assertTrue(violation.get().contains("30%"));
    }

    @Test
    void rejectsInventedYear() {
        assertTrue(validator
                .violation("Worked on payments.", "Worked on payments since 2019.")
                .isPresent());
    }

    @Test
    void rejectsEmptySuggestion() {
        assertTrue(validator.violation("Some text.", "  ").isPresent());
    }

    @Test
    void ignoresNumbersPresentInOriginal() {
        assertEquals(
                Optional.empty(),
                validator.violation("Led a team of 5 engineers.", "Led a team of 5 engineers successfully."));
    }
}
