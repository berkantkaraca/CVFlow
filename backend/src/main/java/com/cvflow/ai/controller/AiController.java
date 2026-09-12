package com.cvflow.ai.controller;

import com.cvflow.ai.dto.AiSuggestionResponse;
import com.cvflow.ai.dto.ImproveRequest;
import com.cvflow.ai.model.AiSuggestionStatus;
import com.cvflow.ai.service.AiService;
import com.cvflow.common.response.ApiResponse;
import com.cvflow.common.response.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/improve")
    public ResponseEntity<ApiResponse<AiSuggestionResponse>> improve(@Valid @RequestBody ImproveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponses.success(aiService.improve(request)));
    }

    @PostMapping("/rewrite")
    public ResponseEntity<ApiResponse<AiSuggestionResponse>> rewrite(@Valid @RequestBody ImproveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponses.success(aiService.rewrite(request)));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<AiSuggestionResponse>>> list(
            @RequestParam UUID languageVersionId, @RequestParam(required = false) AiSuggestionStatus status) {
        return ResponseEntity.ok(ApiResponses.success(aiService.list(languageVersionId, status)));
    }

    @PostMapping("/suggestions/{id}/accept")
    public ResponseEntity<ApiResponse<AiSuggestionResponse>> accept(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponses.success(aiService.accept(id)));
    }

    @PostMapping("/suggestions/{id}/reject")
    public ResponseEntity<ApiResponse<AiSuggestionResponse>> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponses.success(aiService.reject(id)));
    }
}
