package com.cvflow.cv.service;

import com.cvflow.cv.model.CvContent;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class CvContentCloner {
    private final ObjectMapper objectMapper;

    public CvContentCloner(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CvContent clone(CvContent source) {
        if (source == null) {
            return CvContent.empty();
        }
        return objectMapper.convertValue(objectMapper.valueToTree(source), CvContent.class);
    }
}
