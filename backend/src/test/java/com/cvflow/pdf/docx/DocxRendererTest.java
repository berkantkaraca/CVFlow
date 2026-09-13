package com.cvflow.pdf.docx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.Experience;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.PersonalInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

class DocxRendererTest {
    private final DocxRenderer renderer = new DocxRenderer();

    @Test
    void rendersValidDocxWithContent() throws Exception {
        CvContent content = CvContent.empty();
        PersonalInfo personal = new PersonalInfo();
        personal.setFullName("Jane Doe");
        personal.setTitle("Backend Developer");
        content.setPersonal(personal);
        content.setSummary("Experienced developer");
        Experience experience = new Experience();
        experience.setId(UUID.randomUUID());
        experience.setCompany("Acme");
        experience.setPosition("Developer");
        experience.setDescription(new ArrayList<>(List.of("Did things")));
        content.setExperiences(new ArrayList<>(List.of(experience)));

        byte[] docx = renderer.render(content, CvSection.defaultOrder(), LanguageCode.EN);

        assertTrue(docx.length > 0);
        assertEquals('P', docx[0]);
        assertEquals('K', docx[1]);
        try (XWPFDocument document = new XWPFDocument(new java.io.ByteArrayInputStream(docx))) {
            String text = document.getParagraphs().stream()
                    .map(p -> p.getText())
                    .reduce("", (a, b) -> a + "\n" + b);
            assertTrue(text.contains("Jane Doe"));
            assertTrue(text.contains("Experienced developer"));
            assertTrue(text.contains("Acme"));
            assertTrue(text.contains("Did things"));
        }
    }

    @Test
    void rendersEmptyContentWithoutFailing() {
        byte[] docx = renderer.render(CvContent.empty(), List.of(), LanguageCode.TR);

        assertTrue(docx.length > 0);
    }

    @Test
    void respectsSectionOrder() throws Exception {
        CvContent content = CvContent.empty();
        content.setSummary("the summary");
        Experience experience = new Experience();
        experience.setId(UUID.randomUUID());
        experience.setCompany("Acme");
        content.setExperiences(new ArrayList<>(List.of(experience)));

        byte[] docx = renderer.render(content, List.of(CvSection.EXPERIENCE, CvSection.SUMMARY), LanguageCode.EN);

        try (XWPFDocument document = new XWPFDocument(new java.io.ByteArrayInputStream(docx))) {
            String text = document.getParagraphs().stream()
                    .map(p -> p.getText())
                    .reduce("", (a, b) -> a + "\n" + b);
            assertTrue(text.indexOf("Acme") < text.indexOf("the summary"));
        }
    }
}
