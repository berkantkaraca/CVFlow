package com.cvflow.ai.service;

import com.cvflow.cv.model.Certification;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.Education;
import com.cvflow.cv.model.Experience;
import com.cvflow.cv.model.Language;
import com.cvflow.cv.model.Link;
import com.cvflow.cv.model.Project;
import com.cvflow.cv.model.Skill;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CvContentDiffer {
    public record TranslationDiff(
            CvSection section, UUID entityId, String field, String original, String suggested) {}

    public List<TranslationDiff> diff(CvContent translated, CvContent target) {
        List<TranslationDiff> diffs = new ArrayList<>();
        diffValue(diffs, CvSection.SUMMARY, null, "summary", target.getSummary(), translated.getSummary());
        translated.getExperiences().forEach(t -> {
            var current = byId(target.getExperiences(), t.getId(), Experience::getId);
            if (current.isEmpty()) {
                return;
            }
            diffValue(diffs, CvSection.EXPERIENCE, t.getId(), "company", current.get().getCompany(), t.getCompany());
            diffValue(diffs, CvSection.EXPERIENCE, t.getId(), "position", current.get().getPosition(), t.getPosition());
            diffList(diffs, CvSection.EXPERIENCE, t.getId(), current.get().getDescription(), t.getDescription());
        });
        translated.getEducation().forEach(t -> {
            var current = byId(target.getEducation(), t.getId(), Education::getId);
            if (current.isEmpty()) {
                return;
            }
            diffValue(diffs, CvSection.EDUCATION, t.getId(), "institution", current.get().getInstitution(), t.getInstitution());
            diffValue(diffs, CvSection.EDUCATION, t.getId(), "degree", current.get().getDegree(), t.getDegree());
            diffValue(diffs, CvSection.EDUCATION, t.getId(), "field", current.get().getField(), t.getField());
            diffValue(
                    diffs, CvSection.EDUCATION, t.getId(), "description", current.get().getDescription(), t.getDescription());
        });
        translated.getProjects().forEach(t -> {
            var current = byId(target.getProjects(), t.getId(), Project::getId);
            if (current.isEmpty()) {
                return;
            }
            diffValue(diffs, CvSection.PROJECTS, t.getId(), "name", current.get().getName(), t.getName());
            diffValue(
                    diffs, CvSection.PROJECTS, t.getId(), "description", current.get().getDescription(), t.getDescription());
        });
        translated.getSkills().forEach(t -> {
            var current = byId(target.getSkills(), t.getId(), Skill::getId);
            if (current.isEmpty()) {
                return;
            }
            diffValue(diffs, CvSection.SKILLS, t.getId(), "name", current.get().getName(), t.getName());
            diffValue(diffs, CvSection.SKILLS, t.getId(), "category", current.get().getCategory(), t.getCategory());
        });
        translated.getLanguages().forEach(t -> {
            var current = byId(target.getLanguages(), t.getId(), Language::getId);
            if (current.isEmpty()) {
                return;
            }
            diffValue(diffs, CvSection.LANGUAGES, t.getId(), "name", current.get().getName(), t.getName());
            diffValue(diffs, CvSection.LANGUAGES, t.getId(), "level", current.get().getLevel(), t.getLevel());
        });
        translated.getCertifications().forEach(t -> {
            var current = byId(target.getCertifications(), t.getId(), Certification::getId);
            if (current.isEmpty()) {
                return;
            }
            diffValue(
                    diffs, CvSection.CERTIFICATIONS, t.getId(), "name", current.get().getName(), t.getName());
            diffValue(
                    diffs, CvSection.CERTIFICATIONS, t.getId(), "issuer", current.get().getIssuer(), t.getIssuer());
            diffValue(diffs, CvSection.CERTIFICATIONS, t.getId(), "date", current.get().getDate(), t.getDate());
        });
        translated.getLinks().forEach(t -> {
            var current = byId(target.getLinks(), t.getId(), Link::getId);
            if (current.isEmpty()) {
                return;
            }
            diffValue(diffs, CvSection.LINKS, t.getId(), "label", current.get().getLabel(), t.getLabel());
            diffValue(diffs, CvSection.LINKS, t.getId(), "url", current.get().getUrl(), t.getUrl());
        });
        return diffs;
    }

    private void diffValue(
            List<TranslationDiff> diffs,
            CvSection section,
            UUID entityId,
            String field,
            String original,
            String suggested) {
        if (suggested == null || suggested.isBlank() || suggested.equals(original)) {
            return;
        }
        diffs.add(new TranslationDiff(section, entityId, field, original, suggested));
    }

    private void diffList(
            List<TranslationDiff> diffs,
            CvSection section,
            UUID entityId,
            List<String> original,
            List<String> suggested) {
        List<String> current = original == null ? List.of() : original;
        List<String> next = suggested == null ? List.of() : suggested;
        for (int i = 0; i < Math.min(current.size(), next.size()); i++) {
            diffValue(diffs, section, entityId, "description", current.get(i), next.get(i));
        }
    }

    private <T> java.util.Optional<T> byId(List<T> items, UUID id, Function<T, UUID> idOf) {
        if (id == null || items == null) {
            return java.util.Optional.empty();
        }
        Map<UUID, T> index = items.stream()
                .filter(item -> idOf.apply(item) != null)
                .collect(Collectors.toMap(idOf, Function.identity(), (a, b) -> a));
        return java.util.Optional.ofNullable(index.get(id));
    }
}
