package com.cvflow.backup.controller;

import com.cvflow.backup.dto.BackupDtos.BackupDocument;
import com.cvflow.backup.dto.BackupDtos.BackupImportResult;
import com.cvflow.backup.service.BackupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/backup")
@RequiredArgsConstructor
public class BackupController {
    private final BackupService backupService;

    @GetMapping("/export")
    public ResponseEntity<BackupDocument> exportBackup() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("cvflow-backup.json").build().toString())
                .body(backupService.exportBackup());
    }

    @PostMapping("/import")
    public ResponseEntity<BackupImportResult> importBackup(@Valid @RequestBody BackupDocument document) {
        return ResponseEntity.status(201).body(backupService.importBackup(document));
    }
}
