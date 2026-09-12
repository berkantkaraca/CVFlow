package com.cvflow.ai.repository;

import com.cvflow.ai.entity.CvAiSuggestion;
import com.cvflow.ai.model.AiSuggestionStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvAiSuggestionRepository extends JpaRepository<CvAiSuggestion, UUID> {
    List<CvAiSuggestion> findAllByLanguageVersionId(UUID languageVersionId);

    List<CvAiSuggestion> findAllByLanguageVersionIdAndStatus(UUID languageVersionId, AiSuggestionStatus status);
}
