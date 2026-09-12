package com.cvflow.ai.service;

import com.cvflow.ai.model.AiSuggestionPayload;
import com.cvflow.common.exception.InvalidFieldException;
import com.cvflow.common.exception.InvalidSuggestionStateException;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import org.springframework.stereotype.Component;

@Component
public class SuggestionApplier {
    public void apply(CvContent content, AiSuggestionPayload payload) {
        switch (payload.getSection()) {
            case SUMMARY -> applySummary(content, payload);
            case EXPERIENCE -> applyExperience(content, payload);
            case EDUCATION -> applyEducation(content, payload);
            case PROJECTS -> applyProject(content, payload);
            case SKILLS -> applySkill(content, payload);
            case LANGUAGES -> applyLanguage(content, payload);
            case CERTIFICATIONS -> applyCertification(content, payload);
            case LINKS -> applyLink(content, payload);
        }
    }

    private void applySummary(CvContent content, AiSuggestionPayload payload) {
        requireField(payload, "summary");
        content.setSummary(payload.getSuggested());
    }

    private void applyExperience(CvContent content, AiSuggestionPayload payload) {
        var experience = content.getExperiences().stream()
                .filter(e -> e.getId() != null && e.getId().equals(payload.getEntityId()))
                .findFirst()
                .orElseThrow(() -> new InvalidSuggestionStateException());
        switch (payload.getField()) {
            case "company" -> experience.setCompany(payload.getSuggested());
            case "position" -> experience.setPosition(payload.getSuggested());
            case "description" -> {
                int index = experience.getDescription().indexOf(payload.getOriginal());
                if (index < 0) {
                    throw new InvalidSuggestionStateException();
                }
                experience.getDescription().set(index, payload.getSuggested());
            }
            default -> throw new InvalidFieldException("Unsupported field: " + payload.getField());
        }
    }

    private void applyEducation(CvContent content, AiSuggestionPayload payload) {
        var education = content.getEducation().stream()
                .filter(e -> e.getId() != null && e.getId().equals(payload.getEntityId()))
                .findFirst()
                .orElseThrow(() -> new InvalidSuggestionStateException());
        switch (payload.getField()) {
            case "institution" -> education.setInstitution(payload.getSuggested());
            case "degree" -> education.setDegree(payload.getSuggested());
            case "field" -> education.setField(payload.getSuggested());
            case "description" -> education.setDescription(payload.getSuggested());
            default -> throw new InvalidFieldException("Unsupported field: " + payload.getField());
        }
    }

    private void applyProject(CvContent content, AiSuggestionPayload payload) {
        var project = content.getProjects().stream()
                .filter(p -> p.getId() != null && p.getId().equals(payload.getEntityId()))
                .findFirst()
                .orElseThrow(() -> new InvalidSuggestionStateException());
        switch (payload.getField()) {
            case "name" -> project.setName(payload.getSuggested());
            case "description" -> project.setDescription(payload.getSuggested());
            default -> throw new InvalidFieldException("Unsupported field: " + payload.getField());
        }
    }

    private void applySkill(CvContent content, AiSuggestionPayload payload) {
        var skill = content.getSkills().stream()
                .filter(s -> s.getId() != null && s.getId().equals(payload.getEntityId()))
                .findFirst()
                .orElseThrow(() -> new InvalidSuggestionStateException());
        switch (payload.getField()) {
            case "name" -> skill.setName(payload.getSuggested());
            case "category" -> skill.setCategory(payload.getSuggested());
            default -> throw new InvalidFieldException("Unsupported field: " + payload.getField());
        }
    }

    private void applyLanguage(CvContent content, AiSuggestionPayload payload) {
        var language = content.getLanguages().stream()
                .filter(l -> l.getId() != null && l.getId().equals(payload.getEntityId()))
                .findFirst()
                .orElseThrow(() -> new InvalidSuggestionStateException());
        switch (payload.getField()) {
            case "name" -> language.setName(payload.getSuggested());
            case "level" -> language.setLevel(payload.getSuggested());
            default -> throw new InvalidFieldException("Unsupported field: " + payload.getField());
        }
    }

    private void applyCertification(CvContent content, AiSuggestionPayload payload) {
        var certification = content.getCertifications().stream()
                .filter(c -> c.getId() != null && c.getId().equals(payload.getEntityId()))
                .findFirst()
                .orElseThrow(() -> new InvalidSuggestionStateException());
        switch (payload.getField()) {
            case "name" -> certification.setName(payload.getSuggested());
            case "issuer" -> certification.setIssuer(payload.getSuggested());
            case "date" -> certification.setDate(payload.getSuggested());
            default -> throw new InvalidFieldException("Unsupported field: " + payload.getField());
        }
    }

    private void applyLink(CvContent content, AiSuggestionPayload payload) {
        var link = content.getLinks().stream()
                .filter(l -> l.getId() != null && l.getId().equals(payload.getEntityId()))
                .findFirst()
                .orElseThrow(() -> new InvalidSuggestionStateException());
        switch (payload.getField()) {
            case "label" -> link.setLabel(payload.getSuggested());
            case "url" -> link.setUrl(payload.getSuggested());
            default -> throw new InvalidFieldException("Unsupported field: " + payload.getField());
        }
    }

    private void requireField(AiSuggestionPayload payload, String expected) {
        if (!expected.equals(payload.getField())) {
            throw new InvalidFieldException("Unsupported field: " + payload.getField());
        }
        if (payload.getSection() == CvSection.SUMMARY && payload.getEntityId() != null) {
            throw new InvalidFieldException("Section SUMMARY does not take an entity id");
        }
    }
}
