package com.cvflow.pdf.template;

import com.cvflow.cv.model.Certification;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.Education;
import com.cvflow.cv.model.Experience;
import com.cvflow.cv.model.Language;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.Link;
import com.cvflow.cv.model.PersonalInfo;
import com.cvflow.cv.model.Project;
import com.cvflow.cv.model.Skill;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractCvTemplate implements CvTemplate {
    private static final Map<CvSection, String[]> TITLES = Map.of(
            CvSection.SUMMARY, new String[] {"Özet", "Summary"},
            CvSection.EXPERIENCE, new String[] {"Deneyim", "Experience"},
            CvSection.PROJECTS, new String[] {"Projeler", "Projects"},
            CvSection.EDUCATION, new String[] {"Eğitim", "Education"},
            CvSection.SKILLS, new String[] {"Yetenekler", "Skills"},
            CvSection.LANGUAGES, new String[] {"Diller", "Languages"},
            CvSection.CERTIFICATIONS, new String[] {"Sertifikalar", "Certifications"},
            CvSection.LINKS, new String[] {"Bağlantılar", "Links"});

    protected abstract String css();

    protected String headerHtml(PersonalInfo personal) {
        List<String> contacts = contacts(personal);
        StringBuilder html = new StringBuilder();
        html.append("<header class=\"cv-header\">");
        html.append("<h1>").append(esc(nameOf(personal))).append("</h1>");
        if (!isBlank(personal.getTitle())) {
            html.append("<p class=\"cv-title\">").append(esc(personal.getTitle())).append("</p>");
        }
        if (!contacts.isEmpty()) {
            html.append("<p class=\"cv-contacts\">").append(String.join(" &middot; ", contacts)).append("</p>");
        }
        html.append("</header>");
        return html.toString();
    }

    @Override
    public String render(CvContent content, List<CvSection> sectionOrder, LanguageCode language) {
        CvContent safe = content == null ? CvContent.empty() : content;
        List<CvSection> order = sectionOrder == null || sectionOrder.isEmpty()
                ? CvSection.defaultOrder()
                : sectionOrder.stream().distinct().toList();
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><style>")
                .append(css())
                .append("</style></head><body><div class=\"cv-page\">");
        html.append(headerHtml(safe.getPersonal() == null
                ? new PersonalInfo()
                : safe.getPersonal()));
        for (CvSection section : order) {
            String sectionHtml = sectionHtml(section, safe, language);
            if (!sectionHtml.isBlank()) {
                html.append("<section class=\"cv-section cv-")
                        .append(section.name().toLowerCase())
                        .append("\"><h2>")
                        .append(esc(titleOf(section, language)))
                        .append("</h2>")
                        .append(sectionHtml)
                        .append("</section>");
            }
        }
        html.append("</div></body></html>");
        return html.toString();
    }

    protected String titleOf(CvSection section, LanguageCode language) {
        String[] titles = TITLES.get(section);
        return language == LanguageCode.TR ? titles[0] : titles[1];
    }

    private String sectionHtml(CvSection section, CvContent content, LanguageCode language) {
        return switch (section) {
            case SUMMARY -> summaryHtml(content.getSummary());
            case EXPERIENCE -> experiencesHtml(content.getExperiences());
            case PROJECTS -> projectsHtml(content.getProjects());
            case EDUCATION -> educationHtml(content.getEducation());
            case SKILLS -> skillsHtml(content.getSkills());
            case LANGUAGES -> languagesHtml(content.getLanguages());
            case CERTIFICATIONS -> certificationsHtml(content.getCertifications());
            case LINKS -> linksHtml(content.getLinks());
        };
    }

    private String summaryHtml(String summary) {
        if (isBlank(summary)) {
            return "";
        }
        return "<p class=\"cv-summary\">" + esc(summary) + "</p>";
    }

    private String experiencesHtml(List<Experience> experiences) {
        StringBuilder html = new StringBuilder();
        for (Experience e : nullSafe(experiences)) {
            html.append("<div class=\"cv-item\">");
            html.append("<div class=\"cv-item-head\"><span class=\"cv-item-title\">")
                    .append(esc(orDash(e.getPosition())))
                    .append("</span><span class=\"cv-item-meta\">")
                    .append(esc(dateRange(e.getStartDate(), e.getEndDate(), e.isCurrent())))
                    .append("</span></div>");
            List<String> sub = new ArrayList<>();
            if (!isBlank(e.getCompany())) {
                sub.add(esc(e.getCompany()));
            }
            if (!isBlank(e.getLocation())) {
                sub.add(esc(e.getLocation()));
            }
            if (!sub.isEmpty()) {
                html.append("<p class=\"cv-item-sub\">").append(String.join(" &middot; ", sub)).append("</p>");
            }
            if (e.getDescription() != null && !e.getDescription().isEmpty()) {
                html.append("<ul>");
                for (String bullet : e.getDescription()) {
                    if (!isBlank(bullet)) {
                        html.append("<li>").append(esc(bullet)).append("</li>");
                    }
                }
                html.append("</ul>");
            }
            html.append("</div>");
        }
        return html.toString();
    }

    private String projectsHtml(List<Project> projects) {
        StringBuilder html = new StringBuilder();
        for (Project p : nullSafe(projects)) {
            html.append("<div class=\"cv-item\">");
            html.append("<div class=\"cv-item-head\"><span class=\"cv-item-title\">")
                    .append(esc(orDash(p.getName())))
                    .append("</span>");
            if (!isBlank(p.getUrl())) {
                html.append("<span class=\"cv-item-meta\">").append(esc(p.getUrl())).append("</span>");
            }
            html.append("</div>");
            if (!isBlank(p.getDescription())) {
                html.append("<p>").append(esc(p.getDescription())).append("</p>");
            }
            if (p.getTechnologies() != null && !p.getTechnologies().isEmpty()) {
                html.append("<p class=\"cv-tech\">").append(esc(String.join(", ", p.getTechnologies()))).append("</p>");
            }
            html.append("</div>");
        }
        return html.toString();
    }

    private String educationHtml(List<Education> education) {
        StringBuilder html = new StringBuilder();
        for (Education e : nullSafe(education)) {
            html.append("<div class=\"cv-item\">");
            html.append("<div class=\"cv-item-head\"><span class=\"cv-item-title\">")
                    .append(esc(orDash(e.getInstitution())))
                    .append("</span><span class=\"cv-item-meta\">")
                    .append(esc(dateRange(e.getStartDate(), e.getEndDate(), false)))
                    .append("</span></div>");
            List<String> sub = new ArrayList<>();
            if (!isBlank(e.getDegree())) {
                sub.add(esc(e.getDegree()));
            }
            if (!isBlank(e.getField())) {
                sub.add(esc(e.getField()));
            }
            if (!isBlank(e.getLocation())) {
                sub.add(esc(e.getLocation()));
            }
            if (!sub.isEmpty()) {
                html.append("<p class=\"cv-item-sub\">").append(String.join(" &middot; ", sub)).append("</p>");
            }
            if (!isBlank(e.getDescription())) {
                html.append("<p>").append(esc(e.getDescription())).append("</p>");
            }
            html.append("</div>");
        }
        return html.toString();
    }

    private String skillsHtml(List<Skill> skills) {
        StringBuilder html = new StringBuilder();
        for (Skill s : nullSafe(skills)) {
            html.append("<div class=\"cv-inline-item\"><span class=\"cv-item-title\">")
                    .append(esc(orDash(s.getName())))
                    .append("</span>");
            if (!isBlank(s.getCategory())) {
                html.append("<span class=\"cv-item-sub\"> &mdash; ").append(esc(s.getCategory())).append("</span>");
            }
            html.append("</div>");
        }
        return html.toString();
    }

    private String languagesHtml(List<Language> languages) {
        StringBuilder html = new StringBuilder();
        for (Language l : nullSafe(languages)) {
            html.append("<div class=\"cv-inline-item\"><span class=\"cv-item-title\">")
                    .append(esc(orDash(l.getName())))
                    .append("</span>");
            if (!isBlank(l.getLevel())) {
                html.append("<span class=\"cv-item-sub\"> &mdash; ").append(esc(l.getLevel())).append("</span>");
            }
            html.append("</div>");
        }
        return html.toString();
    }

    private String certificationsHtml(List<Certification> certifications) {
        StringBuilder html = new StringBuilder();
        for (Certification c : nullSafe(certifications)) {
            html.append("<div class=\"cv-item\">");
            html.append("<div class=\"cv-item-head\"><span class=\"cv-item-title\">")
                    .append(esc(orDash(c.getName())))
                    .append("</span>");
            if (!isBlank(c.getDate())) {
                html.append("<span class=\"cv-item-meta\">").append(esc(c.getDate())).append("</span>");
            }
            html.append("</div>");
            if (!isBlank(c.getIssuer())) {
                html.append("<p class=\"cv-item-sub\">").append(esc(c.getIssuer())).append("</p>");
            }
            html.append("</div>");
        }
        return html.toString();
    }

    private String linksHtml(List<Link> links) {
        StringBuilder html = new StringBuilder();
        for (Link l : nullSafe(links)) {
            html.append("<div class=\"cv-inline-item\"><span class=\"cv-item-title\">")
                    .append(esc(orDash(l.getLabel())))
                    .append("</span>");
            if (!isBlank(l.getUrl())) {
                html.append("<span class=\"cv-item-sub\"> &mdash; ").append(esc(l.getUrl())).append("</span>");
            }
            html.append("</div>");
        }
        return html.toString();
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
            contacts.add(esc(value));
        }
    }

    private String nameOf(PersonalInfo personal) {
        return isBlank(personal.getFullName()) ? "Untitled CV" : personal.getFullName();
    }

    private String dateRange(String start, String end, boolean current) {
        String from = isBlank(start) ? "" : start;
        String to = current ? "Present" : (isBlank(end) ? "" : end);
        if (from.isEmpty() && to.isEmpty()) {
            return "";
        }
        return from + (to.isEmpty() ? "" : " – " + to);
    }

    private String orDash(String value) {
        return isBlank(value) ? "—" : value;
    }

    protected boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private <T> List<T> nullSafe(List<T> items) {
        return items == null ? List.of() : items;
    }

    protected String esc(String value) {
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
}
