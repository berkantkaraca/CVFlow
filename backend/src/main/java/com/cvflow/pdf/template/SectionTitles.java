package com.cvflow.pdf.template;

import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import java.util.Map;

public final class SectionTitles {
    private static final Map<CvSection, String[]> TITLES = Map.of(
            CvSection.SUMMARY, new String[] {"Özet", "Summary"},
            CvSection.EXPERIENCE, new String[] {"Deneyim", "Experience"},
            CvSection.PROJECTS, new String[] {"Projeler", "Projects"},
            CvSection.EDUCATION, new String[] {"Eğitim", "Education"},
            CvSection.SKILLS, new String[] {"Yetenekler", "Skills"},
            CvSection.LANGUAGES, new String[] {"Diller", "Languages"},
            CvSection.CERTIFICATIONS, new String[] {"Sertifikalar", "Certifications"},
            CvSection.LINKS, new String[] {"Bağlantılar", "Links"});

    private SectionTitles() {}

    public static String titleOf(CvSection section, LanguageCode language) {
        String[] titles = TITLES.get(section);
        return language == LanguageCode.TR ? titles[0] : titles[1];
    }
}
