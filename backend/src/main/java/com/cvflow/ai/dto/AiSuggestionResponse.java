package com.cvflow.ai.dto;

import com.cvflow.ai.model.AiOperation;
import com.cvflow.ai.model.AiSuggestionPayload;
import com.cvflow.ai.model.AiSuggestionStatus;
import java.time.Instant;
import java.util.UUID;

public record AiSuggestionResponse(
        UUID id,
        AiOperation operation,
        AiSuggestionStatus status,
        AiSuggestionPayload payload,
        Instant createdAt) {}
