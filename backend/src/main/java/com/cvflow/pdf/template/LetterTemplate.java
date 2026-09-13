package com.cvflow.pdf.template;

import com.cvflow.cv.entity.CoverLetter;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.PersonalInfo;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LetterTemplate {
    public String render(CoverLetter letter, CvContent content, LanguageCode language) {
        PersonalInfo personal = content != null && content.getPersonal() != null
                ? content.getPersonal()
                : new PersonalInfo();
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><style>")
                .append(CSS)
                .append("</style></head><body><div class=\"letter-page\">");
        html.append("<p class=\"letter-name\">")
                .append(esc(orDash(personal.getFullName())))
                .append("</p>");
        List<String> contacts = contacts(personal);
        if (!contacts.isEmpty()) {
            html.append("<p class=\"letter-contacts\">").append(String.join(" | ", contacts)).append("</p>");
        }
        html.append("<p class=\"letter-date\">").append(LocalDate.now()).append("</p>");
        if (!isBlank(letter.getRecipient()) || !isBlank(letter.getCompany())) {
            html.append("<p class=\"letter-recipient\">");
            if (!isBlank(letter.getRecipient())) {
                html.append(esc(letter.getRecipient())).append("<br>");
            }
            if (!isBlank(letter.getCompany())) {
                html.append(esc(letter.getCompany()));
            }
            html.append("</p>");
        }
        if (!isBlank(letter.getPosition())) {
            html.append("<p class=\"letter-subject\"><strong>Re: ")
                    .append(esc(letter.getPosition()))
                    .append("</strong></p>");
        }
        for (String paragraph : paragraphs(letter.getBody())) {
            html.append("<p>").append(esc(paragraph)).append("</p>");
        }
        html.append("<p class=\"letter-sign\">")
                .append(language == LanguageCode.TR ? "Saygılarımla," : "Sincerely,")
                .append("<br>")
                .append(esc(orDash(personal.getFullName())))
                .append("</p>");
        html.append("</div></body></html>");
        return html.toString();
    }

    private List<String> paragraphs(String body) {
        if (isBlank(body)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String part : body.split("\\r?\\n")) {
            if (!part.isBlank()) {
                result.add(part.trim());
            }
        }
        return result;
    }

    private List<String> contacts(PersonalInfo personal) {
        List<String> contacts = new ArrayList<>();
        add(contacts, personal.getEmail());
        add(contacts, personal.getPhone());
        add(contacts, personal.getLocation());
        return contacts;
    }

    private void add(List<String> contacts, String value) {
        if (!isBlank(value)) {
            contacts.add(esc(value));
        }
    }

    private String orDash(String value) {
        return isBlank(value) ? "—" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String esc(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    private static final String CSS = """
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body { font-family: Georgia, 'Times New Roman', serif; color: #111; font-size: 13px; }
            .letter-page { max-width: 700px; margin: 0 auto; padding: 48px 40px; }
            .letter-name { font-size: 20px; font-weight: bold; }
            .letter-contacts { font-size: 12px; color: #444; margin: 4px 0 24px; }
            .letter-date { margin-bottom: 24px; }
            .letter-recipient { margin-bottom: 16px; }
            .letter-subject { margin-bottom: 16px; }
            .letter-page p { margin-bottom: 12px; line-height: 1.6; }
            .letter-sign { margin-top: 32px; }
            """;
}
