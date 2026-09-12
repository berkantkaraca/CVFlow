package com.cvflow.ai.service;

import com.cvflow.common.exception.InvalidFieldException;
import com.cvflow.cv.model.CvSection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CvFieldValidator {
    private static final Map<CvSection, Set<String>> ALLOWED_FIELDS = Map.of(
            CvSection.SUMMARY, Set.of("summary"),
            CvSection.EXPERIENCE, Set.of("company", "position", "description"),
            CvSection.EDUCATION, Set.of("institution", "degree", "field", "description"),
            CvSection.PROJECTS, Set.of("name", "description"),
            CvSection.SKILLS, Set.of("name", "category"),
            CvSection.LANGUAGES, Set.of("name", "level"),
            CvSection.CERTIFICATIONS, Set.of("name", "issuer", "date"),
            CvSection.LINKS, Set.of("label", "url"));

    public void validate(CvSection section, UUID entityId, String field) {
        Set<String> allowed = ALLOWED_FIELDS.get(section);
        if (allowed == null || !allowed.contains(field)) {
            throw new InvalidFieldException("Field '" + field + "' cannot be modified in section " + section);
        }
        if (section == CvSection.SUMMARY) {
            if (entityId != null) {
                throw new InvalidFieldException("Section SUMMARY does not take an entity id");
            }
        } else if (entityId == null) {
            throw new InvalidFieldException("Section " + section + " requires an entity id");
        }
    }
}
