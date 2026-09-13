package com.cvflow.cv.service;

import com.cvflow.common.security.CvAccessService;
import com.cvflow.cv.dto.response.AtsScoreResponse;
import com.cvflow.cv.dto.response.AtsScoreResponse.AtsCheck;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.model.CvContent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtsService {
    private final CvAccessService cvAccessService;

    @Transactional(readOnly = true)
    public AtsScoreResponse score(UUID cvId, UUID languageVersionId) {
        CvLanguageVersion version = cvAccessService.getOwnedLanguageVersion(languageVersionId);
        if (!version.getMasterCvId().equals(cvAccessService.getOwnedCv(cvId).getId())) {
            throw new com.cvflow.common.exception.LanguageVersionNotFoundException();
        }
        CvContent content = version.getContent() == null ? CvContent.empty() : version.getContent();
        List<AtsCheck> checks = new ArrayList<>();
        int score = 0;
        score += check(checks, "contact-email", "Reachable email address", 10, hasText(content, c -> c.getPersonal() != null ? c.getPersonal().getEmail() : null));
        score += check(checks, "contact-phone", "Phone number present", 5, hasText(content, c -> c.getPersonal() != null ? c.getPersonal().getPhone() : null));
        score += check(checks, "summary", "Professional summary (50-2000 chars)", 10, summaryOk(content));
        score += check(checks, "experience-present", "At least one work experience", 15, !content.getExperiences().isEmpty());
        score += check(checks, "experience-titles", "All experiences have company and position", 10, titlesOk(content));
        score += check(checks, "experience-dates", "All experiences have a start date", 5, datesOk(content));
        score += check(checks, "experience-bullets", "At least 3 experience bullets", 10, bulletCount(content) >= 3);
        score += check(checks, "skills", "At least 3 skills", 10, content.getSkills().size() >= 3);
        score += check(checks, "education", "At least one education entry", 10, !content.getEducation().isEmpty());
        score += check(checks, "languages", "At least one language", 5, !content.getLanguages().isEmpty());
        score += check(checks, "no-photo", "No photo (ATS-safe)", 5, content.getPersonal() == null || isBlank(content.getPersonal().getPhotoUrl()));
        score += check(checks, "links", "At least one link", 5, !content.getLinks().isEmpty());
        return new AtsScoreResponse(Math.min(100, score), checks);
    }

    private int check(List<AtsCheck> checks, String code, String label, int points, boolean passed) {
        checks.add(new AtsCheck(code, label, passed, passed ? "+" + points + " points" : "0 points"));
        return passed ? points : 0;
    }

    private boolean summaryOk(CvContent content) {
        int length = content.getSummary() == null ? 0 : content.getSummary().trim().length();
        return length >= 50 && length <= 2000;
    }

    private boolean titlesOk(CvContent content) {
        if (content.getExperiences().isEmpty()) {
            return false;
        }
        return content.getExperiences().stream()
                .allMatch(e -> !isBlank(e.getCompany()) && !isBlank(e.getPosition()));
    }

    private boolean datesOk(CvContent content) {
        if (content.getExperiences().isEmpty()) {
            return false;
        }
        return content.getExperiences().stream().allMatch(e -> !isBlank(e.getStartDate()));
    }

    private int bulletCount(CvContent content) {
        return content.getExperiences().stream()
                .mapToInt(e -> e.getDescription() == null
                        ? 0
                        : (int) e.getDescription().stream().filter(b -> b != null && !b.isBlank()).count())
                .sum();
    }

    private boolean hasText(CvContent content, java.util.function.Function<CvContent, String> getter) {
        return !isBlank(getter.apply(content));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
