package com.cvflow.pdf.controller;

import com.cvflow.cv.dto.request.DocxExportRequest;
import com.cvflow.pdf.service.DocxExportService;
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
public class DocxController {
    private final DocxExportService docxExportService;

    @PostMapping("/docx")
    public ResponseEntity<byte[]> exportDocx(@PathVariable UUID cvId, @Valid @RequestBody DocxExportRequest request) {
        DocxExportService.DocxDocument document = docxExportService.exportDocx(cvId, request);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(document.filename()).build().toString())
                .contentLength(document.content().length)
                .body(document.content());
    }
}
