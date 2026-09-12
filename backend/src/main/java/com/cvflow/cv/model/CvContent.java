package com.cvflow.cv.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public CvContent() {}

    public CvContent(
            PersonalInfo personal,
            String summary,
            List<Experience> experiences,
            List<Education> education,
            List<Project> projects,
            List<Skill> skills,
            List<Language> languages,
            List<Certification> certifications,
            List<Link> links) {
        this.personal = personal;
        this.summary = summary;
        this.experiences = experiences == null ? new ArrayList<>() : experiences;
        this.education = education == null ? new ArrayList<>() : education;
        this.projects = projects == null ? new ArrayList<>() : projects;
        this.skills = skills == null ? new ArrayList<>() : skills;
        this.languages = languages == null ? new ArrayList<>() : languages;
        this.certifications = certifications == null ? new ArrayList<>() : certifications;
        this.links = links == null ? new ArrayList<>() : links;
    }

    public static CvContent empty() {
        CvContent content = new CvContent();
        content.setPersonal(new PersonalInfo());
        return content;
    }

    public PersonalInfo getPersonal() {
        return personal;
    }

    public void setPersonal(PersonalInfo personal) {
        this.personal = personal;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<Experience> getExperiences() {
        return experiences;
    }

    public void setExperiences(List<Experience> experiences) {
        this.experiences = experiences == null ? new ArrayList<>() : experiences;
    }

    public List<Education> getEducation() {
        return education;
    }

    public void setEducation(List<Education> education) {
        this.education = education == null ? new ArrayList<>() : education;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects == null ? new ArrayList<>() : projects;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills == null ? new ArrayList<>() : skills;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages == null ? new ArrayList<>() : languages;
    }

    public List<Certification> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<Certification> certifications) {
        this.certifications = certifications == null ? new ArrayList<>() : certifications;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links == null ? new ArrayList<>() : links;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CvContent that)) {
            return false;
        }
        return Objects.equals(personal, that.personal)
                && Objects.equals(summary, that.summary)
                && Objects.equals(experiences, that.experiences)
                && Objects.equals(education, that.education)
                && Objects.equals(projects, that.projects)
                && Objects.equals(skills, that.skills)
                && Objects.equals(languages, that.languages)
                && Objects.equals(certifications, that.certifications)
                && Objects.equals(links, that.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                personal, summary, experiences, education, projects, skills, languages, certifications, links);
    }
}
