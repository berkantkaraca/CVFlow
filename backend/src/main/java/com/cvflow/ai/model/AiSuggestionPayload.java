package com.cvflow.ai.model;

import com.cvflow.cv.model.CvSection;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiSuggestionPayload {
    private CvSection section;
    private UUID entityId;
    private String field;
    private String original;
    private String suggested;
}
