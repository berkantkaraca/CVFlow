package com.cvflow.share.controller;

import com.cvflow.common.response.ApiResponse;
import com.cvflow.common.response.ApiResponses;
import com.cvflow.share.dto.ShareDtos.CreateShareRequest;
import com.cvflow.share.dto.ShareDtos.ShareResponse;
import com.cvflow.share.service.ShareService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cvs/{cvId}/languages/{languageVersionId}/share")
@RequiredArgsConstructor
public class ShareController {
    private final ShareService shareService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShareResponse>>> list(
            @PathVariable UUID cvId, @PathVariable UUID languageVersionId) {
        return ResponseEntity.ok(ApiResponses.success(shareService.list(cvId, languageVersionId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShareResponse>> create(
            @PathVariable UUID cvId,
            @PathVariable UUID languageVersionId,
            @Valid @RequestBody(required = false) CreateShareRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.success(shareService.create(cvId, languageVersionId, request)));
    }

    @DeleteMapping("/{shareId}")
    public ResponseEntity<ApiResponse<Void>> revoke(
            @PathVariable UUID cvId,
            @PathVariable UUID languageVersionId,
            @PathVariable UUID shareId) {
        shareService.revoke(cvId, languageVersionId, shareId);
        return ResponseEntity.ok(ApiResponses.success());
    }
}
