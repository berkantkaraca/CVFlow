package com.cvflow.cv.controller;

import com.cvflow.common.response.ApiResponse;
import com.cvflow.common.response.ApiResponses;
import com.cvflow.cv.dto.request.CreateLanguageVersionRequest;
import com.cvflow.cv.dto.request.UpdateLanguageVersionRequest;
import com.cvflow.cv.dto.response.LanguageVersionResponse;
import com.cvflow.cv.dto.response.LanguageVersionSummaryResponse;
import com.cvflow.cv.service.LanguageVersionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cvs/{cvId}/languages")
public class LanguageVersionController {
    private final LanguageVersionService languageVersionService;

    public LanguageVersionController(LanguageVersionService languageVersionService) {
        this.languageVersionService = languageVersionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LanguageVersionSummaryResponse>>> list(@PathVariable UUID cvId) {
        return ResponseEntity.ok(ApiResponses.success(languageVersionService.list(cvId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LanguageVersionResponse>> create(
            @PathVariable UUID cvId, @Valid @RequestBody CreateLanguageVersionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.success(languageVersionService.create(cvId, request)));
    }

    @GetMapping("/{languageVersionId}")
    public ResponseEntity<ApiResponse<LanguageVersionResponse>> get(
            @PathVariable UUID cvId, @PathVariable UUID languageVersionId) {
        return ResponseEntity.ok(ApiResponses.success(languageVersionService.get(cvId, languageVersionId)));
    }

    @PutMapping("/{languageVersionId}")
    public ResponseEntity<ApiResponse<LanguageVersionResponse>> update(
            @PathVariable UUID cvId,
            @PathVariable UUID languageVersionId,
            @Valid @RequestBody UpdateLanguageVersionRequest request) {
        return ResponseEntity.ok(
                ApiResponses.success(languageVersionService.update(cvId, languageVersionId, request)));
    }

    @DeleteMapping("/{languageVersionId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID cvId, @PathVariable UUID languageVersionId) {
        languageVersionService.delete(cvId, languageVersionId);
        return ResponseEntity.ok(ApiResponses.success());
    }
}
