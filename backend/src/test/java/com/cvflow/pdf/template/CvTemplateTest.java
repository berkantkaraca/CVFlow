package com.cvflow.pdf.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.Experience;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.PersonalInfo;
import com.cvflow.cv.model.TemplateCode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CvTemplateTest {
    private final TemplateRenderer renderer = new TemplateRenderer(
            List.of(new ModernCvTemplate(), new MinimalCvTemplate(), new ProfessionalCvTemplate(), new AtsTemplate()));

    @Test
    void allTemplatesRenderWithoutData() {
        for (TemplateCode code : TemplateCode.values()) {
            String html = renderer.render(CvContent.empty(), CvSection.defaultOrder(), code, LanguageCode.EN);
            assertTrue(html.contains("<html>"));
            assertTrue(html.contains("Untitled CV"));
        }
    }

    @Test
    void respectsSectionOrder() {
        CvContent content = CvContent.empty();
        content.setSummary("hello");
        Experience experience = new Experience();
        experience.setId(UUID.randomUUID());
        experience.setCompany("Acme");
        content.setExperiences(new ArrayList<>(List.of(experience)));

        String html = renderer.render(
                content, List.of(CvSection.EXPERIENCE, CvSection.SUMMARY), TemplateCode.MODERN, LanguageCode.EN);

        assertTrue(html.indexOf("Acme") < html.indexOf("hello"));
    }

    @Test
    void escapesUserContent() {
        CvContent content = CvContent.empty();
        content.setSummary("<script>alert(1)</script>");

        String html = renderer.render(content, List.of(CvSection.SUMMARY), TemplateCode.MODERN, LanguageCode.EN);

        assertTrue(html.contains("&lt;script&gt;"));
        assertTrue(!html.contains("<script>alert"));
    }

    @Test
    void localizesSectionTitles() {
        CvContent content = CvContent.empty();
        content.setSummary("hello");
        String tr =
                renderer.render(content, List.of(CvSection.SUMMARY), TemplateCode.MINIMAL, LanguageCode.TR);
        String en =
                renderer.render(content, List.of(CvSection.SUMMARY), TemplateCode.MINIMAL, LanguageCode.EN);

        assertTrue(tr.contains("Özet"));
        assertTrue(en.contains("Summary"));
    }

    @Test
    void rendersPersonalHeader() {
        CvContent content = CvContent.empty();
        PersonalInfo personal = new PersonalInfo();
        personal.setFullName("Jane Doe");
        personal.setTitle("Backend Developer");
        personal.setEmail("jane@example.com");
        content.setPersonal(personal);

        String html = renderer.render(content, CvSection.defaultOrder(), TemplateCode.PROFESSIONAL, LanguageCode.EN);

        assertTrue(html.contains("Jane Doe"));
        assertTrue(html.contains("Backend Developer"));
        assertTrue(html.contains("jane@example.com"));
    }
    @Test
    void templatesHaveDistinctStyles() {
        String modern = renderer.render(CvContent.empty(), CvSection.defaultOrder(), TemplateCode.MODERN, LanguageCode.EN);
        String minimal =
                renderer.render(CvContent.empty(), CvSection.defaultOrder(), TemplateCode.MINIMAL, LanguageCode.EN);
        String professional = renderer.render(
                CvContent.empty(), CvSection.defaultOrder(), TemplateCode.PROFESSIONAL, LanguageCode.EN);
        String ats = renderer.render(CvContent.empty(), CvSection.defaultOrder(), TemplateCode.ATS, LanguageCode.EN);

        assertEquals(4, List.of(modern, minimal, professional, ats).stream().distinct().count());
    }

    @Test
    void atsTemplateIsPlain() {
        String ats = renderer.render(CvContent.empty(), CvSection.defaultOrder(), TemplateCode.ATS, LanguageCode.EN);

        assertTrue(!ats.contains("1d4ed8"));
        assertTrue(ats.contains("Arial"));
    }
}
