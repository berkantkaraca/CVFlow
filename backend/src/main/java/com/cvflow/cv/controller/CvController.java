package com.cvflow.cv.controller;

import com.cvflow.common.response.ApiResponse;
import com.cvflow.common.response.ApiResponses;
import com.cvflow.cv.dto.request.BulkDeleteRequest;
import com.cvflow.cv.dto.request.CreateCvRequest;
import com.cvflow.cv.dto.request.RestoreRevisionRequest;
import com.cvflow.cv.dto.request.UpdateCvRequest;
import com.cvflow.cv.dto.response.BulkDeleteResponse;
import com.cvflow.cv.dto.response.CvResponse;
import com.cvflow.cv.dto.response.CvSummaryResponse;
import com.cvflow.cv.dto.response.RevisionResponse;
import com.cvflow.cv.service.CvService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cvs")
@RequiredArgsConstructor
public class CvController {
    private final CvService cvService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CvSummaryResponse>>> list() {
        return ResponseEntity.ok(ApiResponses.success(cvService.list()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CvResponse>> create(@Valid @RequestBody CreateCvRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponses.success(cvService.create(request)));
    }

    @GetMapping("/{cvId}")
    public ResponseEntity<ApiResponse<CvResponse>> get(@PathVariable UUID cvId) {
        return ResponseEntity.ok(ApiResponses.success(cvService.get(cvId)));
    }

    @PutMapping("/{cvId}")
    public ResponseEntity<ApiResponse<CvResponse>> update(
            @PathVariable UUID cvId, @Valid @RequestBody UpdateCvRequest request) {
        return ResponseEntity.ok(ApiResponses.success(cvService.update(cvId, request)));
    }

    @DeleteMapping("/{cvId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID cvId) {
        cvService.delete(cvId);
        return ResponseEntity.ok(ApiResponses.success());
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<BulkDeleteResponse>> bulkDelete(
            @Valid @RequestBody BulkDeleteRequest request) {
        return ResponseEntity.ok(ApiResponses.success(cvService.bulkDelete(request)));
    }

    @PostMapping("/{cvId}/duplicate")
    public ResponseEntity<ApiResponse<CvResponse>> duplicate(@PathVariable UUID cvId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponses.success(cvService.duplicate(cvId)));
    }

    @GetMapping("/{cvId}/revisions")
    public ResponseEntity<ApiResponse<List<RevisionResponse>>> listRevisions(@PathVariable UUID cvId) {
        return ResponseEntity.ok(ApiResponses.success(cvService.listRevisions(cvId)));
    }

    @PostMapping("/{cvId}/revisions/{revisionId}/restore")
    public ResponseEntity<ApiResponse<CvResponse>> restoreRevision(
            @PathVariable UUID cvId,
            @PathVariable UUID revisionId,
            @Valid @RequestBody RestoreRevisionRequest request) {
        return ResponseEntity.ok(ApiResponses.success(cvService.restoreRevision(cvId, revisionId, request)));
    }
}
