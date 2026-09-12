package com.cvflow.pdf.template;

import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TemplateRenderer {
    private final Map<TemplateCode, CvTemplate> templates;

    public TemplateRenderer(List<CvTemplate> templateBeans) {
        Map<TemplateCode, CvTemplate> map = new EnumMap<>(TemplateCode.class);
        for (CvTemplate template : templateBeans) {
            map.put(template.code(), template);
        }
        this.templates = map;
    }

    public String render(CvContent content, List<CvSection> sectionOrder, TemplateCode code, LanguageCode language) {
        CvTemplate template = templates.get(code == null ? TemplateCode.MODERN : code);
        if (template == null) {
            template = templates.get(TemplateCode.MODERN);
        }
        return template.render(content, sectionOrder, language == null ? LanguageCode.EN : language);
    }
}
