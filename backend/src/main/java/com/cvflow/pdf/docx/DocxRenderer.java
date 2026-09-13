package com.cvflow.pdf.docx;

import com.cvflow.common.exception.DocxGenerationException;
import com.cvflow.cv.model.Certification;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.CustomSection;
import com.cvflow.cv.model.Education;
import com.cvflow.cv.model.Experience;
import com.cvflow.cv.model.Language;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.Link;
import com.cvflow.cv.model.PersonalInfo;
import com.cvflow.cv.model.Project;
import com.cvflow.cv.model.Skill;
import com.cvflow.pdf.template.SectionTitles;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

/**
 * Renders CV content as a plain single-column DOCX document (ATS-friendly: standard headings,
 * plain paragraphs, bullet characters instead of native numbering).
 */
@Component
public class DocxRenderer {
    public byte[] render(CvContent content, List<CvSection> sectionOrder, LanguageCode language) {
        CvContent safe = content == null ? CvContent.empty() : content;
        List<CvSection> order = sectionOrder == null || sectionOrder.isEmpty()
                ? CvSection.defaultOrder()
                : sectionOrder.stream().distinct().toList();
        try (XWPFDocument document = new XWPFDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PersonalInfo personal = safe.getPersonal() == null ? new PersonalInfo() : safe.getPersonal();
            heading(document, isBlank(personal.getFullName()) ? "Untitled CV" : personal.getFullName(), 0);
            if (!isBlank(personal.getTitle())) {
                paragraph(document, personal.getTitle(), false, 12);
            }
            List<String> contacts = contacts(personal);
            if (!contacts.isEmpty()) {
                paragraph(document, String.join(" | ", contacts), false, 10);
            }
            for (CvSection section : order) {
                List<String> lines = sectionLines(section, safe);
                if (lines.isEmpty()) {
                    continue;
                }
                heading(document, SectionTitles.titleOf(section, language == null ? LanguageCode.EN : language), 1);
                for (String line : lines) {
                    paragraph(document, line, line.startsWith("• "), 11);
                }
            }
            for (CustomSection custom : nullSafe(safe.getCustomSections())) {
                if (custom.getItems() == null || custom.getItems().isEmpty()) {
                    continue;
                }
                heading(document, isBlank(custom.getTitle()) ? "—" : custom.getTitle(), 1);
                for (String item : custom.getItems()) {
                    if (!isBlank(item)) {
                        paragraph(document, "• " + item, true, 11);
                    }
                }
            }
            document.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new DocxGenerationException("CV could not be exported as DOCX");
        }
    }

    private List<String> sectionLines(CvSection section, CvContent content) {
        return switch (section) {
            case SUMMARY -> isBlank(content.getSummary()) ? List.of() : List.of(content.getSummary());
            case EXPERIENCE -> experienceLines(nullSafe(content.getExperiences()));
            case PROJECTS -> projectLines(nullSafe(content.getProjects()));
            case EDUCATION -> educationLines(nullSafe(content.getEducation()));
            case SKILLS -> inlineLines(nullSafe(content.getSkills()));
            case LANGUAGES -> languageLines(nullSafe(content.getLanguages()));
            case CERTIFICATIONS -> certificationLines(nullSafe(content.getCertifications()));
            case LINKS -> linkLines(nullSafe(content.getLinks()));
        };
    }

    private List<String> experienceLines(List<Experience> experiences) {
        List<String> lines = new ArrayList<>();
        for (Experience e : experiences) {
            String head = join(" — ", orEmpty(e.getPosition()), orEmpty(e.getCompany()));
            String dates = dateRange(e.getStartDate(), e.getEndDate(), e.isCurrent());
            lines.add(dates.isEmpty() ? head : head + " (" + dates + ")");
            if (!isBlank(e.getLocation())) {
                lines.add(e.getLocation());
            }
            for (String bullet : nullSafe(e.getDescription())) {
                if (!isBlank(bullet)) {
                    lines.add("• " + bullet);
                }
            }
        }
        return lines.stream().filter(line -> !line.isBlank()).toList();
    }

    private List<String> projectLines(List<Project> projects) {
        List<String> lines = new ArrayList<>();
        for (Project p : projects) {
            if (!isBlank(p.getName())) {
                lines.add(p.getName());
            }
            if (!isBlank(p.getDescription())) {
                lines.add(p.getDescription());
            }
            if (p.getTechnologies() != null && !p.getTechnologies().isEmpty()) {
                lines.add(String.join(", ", p.getTechnologies()));
            }
        }
        return lines.stream().filter(line -> !line.isBlank()).toList();
    }

    private List<String> educationLines(List<Education> education) {
        List<String> lines = new ArrayList<>();
        for (Education e : education) {
            String head = join(", ", orEmpty(e.getInstitution()), join(", ", orEmpty(e.getDegree()), orEmpty(e.getField())));
            String dates = dateRange(e.getStartDate(), e.getEndDate(), false);
            lines.add(dates.isEmpty() ? head : head + " (" + dates + ")");
            if (!isBlank(e.getDescription())) {
                lines.add(e.getDescription());
            }
        }
        return lines.stream().filter(line -> !line.isBlank()).toList();
    }

    private List<String> inlineLines(List<Skill> skills) {
        List<String> lines = new ArrayList<>();
        for (Skill s : skills) {
            String line = join(" — ", orEmpty(s.getName()), orEmpty(s.getCategory()));
            if (!line.isBlank()) {
                lines.add(line);
            }
        }
        return lines;
    }

    private List<String> languageLines(List<Language> languages) {
        List<String> lines = new ArrayList<>();
        for (Language l : languages) {
            String line = join(" — ", orEmpty(l.getName()), orEmpty(l.getLevel()));
            if (!line.isBlank()) {
                lines.add(line);
            }
        }
        return lines;
    }

    private List<String> certificationLines(List<Certification> certifications) {
        List<String> lines = new ArrayList<>();
        for (Certification c : certifications) {
            String head = join(" — ", orEmpty(c.getName()), orEmpty(c.getIssuer()));
            lines.add(isBlank(c.getDate()) ? head : head + " (" + c.getDate() + ")");
        }
        return lines.stream().filter(line -> !line.isBlank()).toList();
    }

    private List<String> linkLines(List<Link> links) {
        List<String> lines = new ArrayList<>();
        for (Link l : links) {
            String line = join(" — ", orEmpty(l.getLabel()), orEmpty(l.getUrl()));
            if (!line.isBlank()) {
                lines.add(line);
            }
        }
        return lines;
    }

    private void heading(XWPFDocument document, String text, int level) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setStyle(level == 0 ? "Title" : "Heading1");
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(true);
        run.setFontSize(level == 0 ? 22 : 13);
        run.setFontFamily("Calibri");
    }

    private void paragraph(XWPFDocument document, String text, boolean bullet, int fontSize) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(fontSize);
        run.setFontFamily("Calibri");
        if (bullet) {
            paragraph.setIndentationLeft(360);
        }
    }

    private List<String> contacts(PersonalInfo personal) {
        List<String> contacts = new ArrayList<>();
        addContact(contacts, personal.getEmail());
        addContact(contacts, personal.getPhone());
        addContact(contacts, personal.getLocation());
        addContact(contacts, personal.getWebsite());
        addContact(contacts, personal.getLinkedin());
        addContact(contacts, personal.getGithub());
        return contacts;
    }

    private void addContact(List<String> contacts, String value) {
        if (!isBlank(value)) {
            contacts.add(value);
        }
    }

    private String dateRange(String start, String end, boolean current) {
        String from = orEmpty(start);
        String to = current ? "Present" : orEmpty(end);
        if (from.isEmpty() && to.isEmpty()) {
            return "";
        }
        return to.isEmpty() ? from : from + " - " + to;
    }

    private String join(String delimiter, String... parts) {
        List<String> present = new ArrayList<>();
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                present.add(part);
            }
        }
        return String.join(delimiter, present);
    }

    private String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private <T> List<T> nullSafe(List<T> items) {
        return items == null ? List.of() : items;
    }
}
