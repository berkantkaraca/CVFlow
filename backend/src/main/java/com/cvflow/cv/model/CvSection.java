package com.cvflow.cv.model;

import java.util.List;

public enum CvSection {
    SUMMARY,
    EXPERIENCE,
    PROJECTS,
    EDUCATION,
    SKILLS,
    LANGUAGES,
    CERTIFICATIONS,
    LINKS;

    public static List<CvSection> defaultOrder() {
        return List.of(
                SUMMARY,
                EXPERIENCE,
                PROJECTS,
                EDUCATION,
                SKILLS,
                LANGUAGES,
                CERTIFICATIONS,
                LINKS);
    }
}
