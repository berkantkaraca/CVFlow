package com.cvflow.ai.mapper;

import com.cvflow.ai.dto.AiSuggestionResponse;
import com.cvflow.ai.entity.CvAiSuggestion;

public final class AiSuggestionMapper {
    private AiSuggestionMapper() {}

    public static AiSuggestionResponse toResponse(CvAiSuggestion suggestion) {
        return new AiSuggestionResponse(
                suggestion.getId(),
                suggestion.getOperation(),
                suggestion.getStatus(),
                suggestion.getPayload(),
                suggestion.getCreatedAt());
    }
}
