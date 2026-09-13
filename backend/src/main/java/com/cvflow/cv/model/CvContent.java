package com.cvflow.cv.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CvContent {
    private PersonalInfo personal;
    private String summary;
    private List<Experience> experiences = new ArrayList<>();
    private List<Education> education = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();
    private List<Skill> skills = new ArrayList<>();
    private List<Language> languages = new ArrayList<>();
    private List<Certification> certifications = new ArrayList<>();
    private List<Link> links = new ArrayList<>();
    private List<CustomSection> customSections = new ArrayList<>();

    public static CvContent empty() {
        CvContent content = new CvContent();
        content.setPersonal(new PersonalInfo());
        return content;
    }
}
