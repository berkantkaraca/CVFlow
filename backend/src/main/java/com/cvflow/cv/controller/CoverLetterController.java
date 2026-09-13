package com.cvflow.cv.controller;

import com.cvflow.common.response.ApiResponse;
import com.cvflow.common.response.ApiResponses;
import com.cvflow.cv.dto.request.CreateCoverLetterRequest;
import com.cvflow.cv.dto.request.UpdateCoverLetterRequest;
import com.cvflow.cv.dto.response.CoverLetterResponse;
import com.cvflow.cv.dto.response.CoverLetterSummaryResponse;
import com.cvflow.cv.service.CoverLetterService;
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
@RequestMapping("/api/v1/cvs/{cvId}/cover-letters")
@RequiredArgsConstructor
public class CoverLetterController {
    private final CoverLetterService coverLetterService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CoverLetterSummaryResponse>>> list(@PathVariable UUID cvId) {
        return ResponseEntity.ok(ApiResponses.success(coverLetterService.list(cvId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CoverLetterResponse>> create(
            @PathVariable UUID cvId, @Valid @RequestBody CreateCoverLetterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.success(coverLetterService.create(cvId, request)));
    }

    @GetMapping("/{letterId}")
    public ResponseEntity<ApiResponse<CoverLetterResponse>> get(
            @PathVariable UUID cvId, @PathVariable UUID letterId) {
        return ResponseEntity.ok(ApiResponses.success(coverLetterService.get(cvId, letterId)));
    }

    @PutMapping("/{letterId}")
    public ResponseEntity<ApiResponse<CoverLetterResponse>> update(
            @PathVariable UUID cvId,
            @PathVariable UUID letterId,
            @Valid @RequestBody UpdateCoverLetterRequest request) {
        return ResponseEntity.ok(ApiResponses.success(coverLetterService.update(cvId, letterId, request)));
    }

    @DeleteMapping("/{letterId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID cvId, @PathVariable UUID letterId) {
        coverLetterService.delete(cvId, letterId);
        return ResponseEntity.ok(ApiResponses.success());
    }

    @PostMapping("/{letterId}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable UUID cvId, @PathVariable UUID letterId) {
        CoverLetterService.PdfLetter document = coverLetterService.exportPdf(cvId, letterId);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .header(
                        org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        org.springframework.http.ContentDisposition.attachment()
                                .filename(document.filename())
                                .build()
                                .toString())
                .contentLength(document.content().length)
                .body(document.content());
    }
}
