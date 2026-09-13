package com.cvflow.pdf.template;

import com.cvflow.cv.model.TemplateCode;
import org.springframework.stereotype.Component;

/**
 * Ultra-plain single-column template for applicant tracking systems: no colors, no graphics,
 * no multi-column layout — only standard headings and plain paragraphs.
 */
@Component
public class AtsTemplate extends AbstractCvTemplate {
    @Override
    public TemplateCode code() {
        return TemplateCode.ATS;
    }

    @Override
    protected String css() {
        return """
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body { font-family: Arial, Helvetica, sans-serif; color: #000000; font-size: 12px; }
                .cv-page { max-width: 760px; margin: 0 auto; padding: 24px 32px; }
                .cv-header { margin-bottom: 16px; }
                .cv-header h1 { font-size: 22px; }
                .cv-title { font-size: 13px; margin-top: 2px; }
                .cv-contacts { font-size: 11px; margin-top: 4px; }
                .cv-section { margin-bottom: 14px; }
                .cv-section h2 { font-size: 13px; text-transform: uppercase; margin-bottom: 6px; }
                .cv-item { margin-bottom: 8px; }
                .cv-item-head { margin-bottom: 2px; }
                .cv-item-title { font-weight: bold; }
                .cv-item-meta { font-size: 11px; }
                .cv-item-sub { font-size: 11px; margin: 2px 0; }
                .cv-inline-item { margin-bottom: 2px; }
                ul { margin: 2px 0 2px 18px; }
                li { margin-bottom: 1px; }
                .cv-tech { font-size: 11px; }
                """;
    }
}
