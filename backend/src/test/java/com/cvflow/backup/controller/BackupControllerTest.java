package com.cvflow.backup.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cvflow.auth.service.JwtService;
import com.cvflow.backup.dto.BackupDtos.BackupDocument;
import com.cvflow.backup.dto.BackupDtos.BackupImportResult;
import com.cvflow.backup.service.BackupService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BackupController.class)
@AutoConfigureMockMvc(addFilters = false)
class BackupControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BackupService backupService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void exportReturnsAttachment() throws Exception {
        when(backupService.exportBackup())
                .thenReturn(new BackupDocument(1, Instant.now(), "user@example.com", List.of()));

        mvc.perform(get("/api/v1/backup/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                        header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("cvflow-backup.json")));
    }

    @Test
    void importReturnsCounts() throws Exception {
        when(backupService.importBackup(any())).thenReturn(new BackupImportResult(2, 3));

        mvc.perform(post("/api/v1/backup/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"format\":1,\"masterCvs\":[]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.importedCvs").value(2))
                .andExpect(jsonPath("$.importedLanguages").value(3));
    }
}
