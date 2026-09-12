package com.cvflow.cv.service;

import com.cvflow.common.exception.InvalidSectionOrderException;
import com.cvflow.cv.model.CvSection;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SectionOrderValidator {
    public void validate(List<CvSection> sectionOrder) {
        if (sectionOrder == null || sectionOrder.isEmpty()) {
            throw new InvalidSectionOrderException("Section order must not be empty");
        }
        if (new HashSet<>(sectionOrder).size() != sectionOrder.size()) {
            throw new InvalidSectionOrderException("Section order must not contain duplicates");
        }
    }
}
