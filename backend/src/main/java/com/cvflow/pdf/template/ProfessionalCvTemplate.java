package com.cvflow.pdf.template;

import com.cvflow.cv.model.TemplateCode;
import org.springframework.stereotype.Component;

@Component
public class ProfessionalCvTemplate extends AbstractCvTemplate {
    @Override
    public TemplateCode code() {
        return TemplateCode.PROFESSIONAL;
    }

    @Override
    protected String css() {
        return """
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body { font-family: Calibri, Arial, Helvetica, sans-serif; color: #0f172a; font-size: 13px; }
                .cv-page { max-width: 780px; margin: 0 auto; padding: 28px 36px; }
                .cv-header { border-bottom: 3px solid #0f172a; padding-bottom: 12px; margin-bottom: 18px; }
                .cv-header h1 { font-size: 24px; }
                .cv-title { font-size: 14px; color: #334155; margin-top: 2px; }
                .cv-contacts { font-size: 12px; color: #475569; margin-top: 6px; }
                .cv-section { margin-bottom: 16px; }
                .cv-section h2 { font-size: 13px; text-transform: uppercase; color: #0f172a;
                  border-bottom: 1px solid #94a3b8; padding-bottom: 3px; margin-bottom: 8px; }
                .cv-item { margin-bottom: 10px; }
                .cv-item-head { display: flex; justify-content: space-between; gap: 12px; }
                .cv-item-title { font-weight: 600; }
                .cv-item-meta { color: #475569; font-size: 12px; white-space: nowrap; }
                .cv-item-sub { color: #334155; font-size: 12px; margin: 2px 0; }
                .cv-inline-item { margin-bottom: 3px; }
                ul { margin: 4px 0 4px 18px; }
                li { margin-bottom: 2px; }
                .cv-tech { color: #475569; font-size: 12px; }
                """;
    }
}
