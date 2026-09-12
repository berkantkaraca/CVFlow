package com.cvflow.pdf.template;

import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TemplateRenderer {
    private final List<CvTemplate> templates;

    public String render(CvContent content, List<CvSection> sectionOrder, TemplateCode code, LanguageCode language) {
        TemplateCode effective = code == null ? TemplateCode.MODERN : code;
        return templates.stream()
                .filter(template -> template.code() == effective)
                .findFirst()
                .orElseGet(() -> templates.stream()
                        .filter(template -> template.code() == TemplateCode.MODERN)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No CV templates registered")))
                .render(content, sectionOrder, language == null ? LanguageCode.EN : language);
    }
}
