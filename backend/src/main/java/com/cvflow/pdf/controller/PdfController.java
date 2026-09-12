package com.cvflow.pdf.controller;

import com.cvflow.cv.dto.request.PdfExportRequest;
import com.cvflow.pdf.service.PdfExportService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cvs/{cvId}/export")
@RequiredArgsConstructor
public class PdfController {
    private final PdfExportService pdfExportService;

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable UUID cvId, @Valid @RequestBody PdfExportRequest request) {
        PdfExportService.PdfDocument document = pdfExportService.exportPdf(cvId, request);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(document.filename()).build().toString())
                .contentLength(document.content().length)
                .body(document.content());
    }
}
