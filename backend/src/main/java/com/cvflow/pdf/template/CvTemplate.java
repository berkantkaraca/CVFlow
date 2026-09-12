package com.cvflow.pdf.template;

import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import java.util.List;

public interface CvTemplate {
    TemplateCode code();

    String render(CvContent content, List<CvSection> sectionOrder, LanguageCode language);
}
