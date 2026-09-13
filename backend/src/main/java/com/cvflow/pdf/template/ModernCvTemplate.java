package com.cvflow.pdf.template;

import com.cvflow.cv.model.TemplateCode;
import org.springframework.stereotype.Component;

@Component
public class ModernCvTemplate extends AbstractCvTemplate {
    @Override
    public TemplateCode code() {
        return TemplateCode.MODERN;
    }

    @Override
    protected String css() {
        return """
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body { font-family: 'Segoe UI', Arial, Helvetica, sans-serif; color: #1f2937; font-size: 13px; }
                .cv-page { max-width: 780px; margin: 0 auto; padding: 32px 36px; }
                .cv-header { background: #1d4ed8; color: #ffffff; border-radius: 8px; padding: 24px 28px; margin-bottom: 24px; }
                .cv-header h1 { font-size: 28px; margin-bottom: 4px; }
                .cv-photo { width: 96px; height: 96px; object-fit: cover; border-radius: 50%; margin-bottom: 8px; }
                .cv-title { font-size: 15px; opacity: 0.9; }
                .cv-contacts { font-size: 12px; margin-top: 8px; opacity: 0.9; }
                .cv-section { margin-bottom: 20px; }
                .cv-section h2 { font-size: 14px; text-transform: uppercase; letter-spacing: 1px; color: #1d4ed8;
                  border-bottom: 2px solid #dbeafe; padding-bottom: 4px; margin-bottom: 10px; }
                .cv-item { margin-bottom: 12px; }
                .cv-item-head { display: flex; justify-content: space-between; gap: 12px; }
                .cv-item-title { font-weight: 600; }
                .cv-item-meta { color: #6b7280; font-size: 12px; white-space: nowrap; }
                .cv-item-sub { color: #4b5563; font-size: 12px; margin: 2px 0; }
                .cv-inline-item { margin-bottom: 4px; }
                ul { margin: 4px 0 4px 18px; }
                li { margin-bottom: 2px; }
                .cv-tech { color: #6b7280; font-size: 12px; }
                """;
    }
}
