package com.cvflow.pdf.template;

import com.cvflow.cv.model.PersonalInfo;
import com.cvflow.cv.model.TemplateCode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MinimalCvTemplate extends AbstractCvTemplate {
    @Override
    public TemplateCode code() {
        return TemplateCode.MINIMAL;
    }

    @Override
    protected String headerHtml(PersonalInfo personal) {
        String name = isBlank(personal.getFullName()) ? "Untitled CV" : personal.getFullName();
        return """
                <header class="cv-header"><h1>%s</h1>%s%s</header>
                """.formatted(
                esc(name),
                isBlank(personal.getTitle()) ? "" : "<p class=\"cv-title\">" + esc(personal.getTitle()) + "</p>",
                contactsLine(personal));
    }

    @Override
    protected String css() {
        return """
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body { font-family: Georgia, 'Times New Roman', serif; color: #111111; font-size: 13px; }
                .cv-page { max-width: 720px; margin: 0 auto; padding: 40px 32px; }
                .cv-header { text-align: center; margin-bottom: 28px; }
                .cv-header h1 { font-size: 26px; font-weight: normal; letter-spacing: 2px; }
                .cv-photo { width: 96px; height: 96px; object-fit: cover; border-radius: 50%; }
                .cv-title { font-style: italic; margin-top: 4px; }
                .cv-contacts { font-size: 12px; margin-top: 6px; }
                .cv-section { margin-bottom: 22px; }
                .cv-section h2 { font-size: 13px; font-weight: normal; text-transform: uppercase; letter-spacing: 3px;
                  text-align: center; margin-bottom: 12px; }
                .cv-item { margin-bottom: 12px; }
                .cv-item-head { display: flex; justify-content: space-between; gap: 12px; }
                .cv-item-title { font-weight: bold; }
                .cv-item-meta { font-size: 12px; white-space: nowrap; }
                .cv-item-sub { font-size: 12px; margin: 2px 0; }
                .cv-inline-item { margin-bottom: 4px; }
                ul { margin: 4px 0 4px 18px; }
                li { margin-bottom: 2px; }
                .cv-tech { font-size: 12px; font-style: italic; }
                """;
    }

    private String contactsLine(PersonalInfo personal) {
        List<String> contacts = new java.util.ArrayList<>();
        add(contacts, personal.getEmail());
        add(contacts, personal.getPhone());
        add(contacts, personal.getLocation());
        add(contacts, personal.getWebsite());
        add(contacts, personal.getLinkedin());
        add(contacts, personal.getGithub());
        if (contacts.isEmpty()) {
            return "";
        }
        return "<p class=\"cv-contacts\">" + String.join(" | ", contacts) + "</p>";
    }

    private void add(List<String> contacts, String value) {
        if (!isBlank(value)) {
            contacts.add(esc(value));
        }
    }
}
