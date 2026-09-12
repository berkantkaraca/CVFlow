package com.cvflow.pdf.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cvflow.common.exception.LanguageVersionNotFoundException;
import com.cvflow.pdf.service.PdfExportService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PdfController.class)
@AutoConfigureMockMvc(addFilters = false)
class PdfControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private com.cvflow.auth.service.JwtService jwtService;

    @MockitoBean
    private PdfExportService pdfExportService;

    private final UUID cvId = UUID.randomUUID();

    @Test
    void exportReturnsPdfAttachment() throws Exception {
        when(pdfExportService.exportPdf(eq(cvId), any()))
                .thenReturn(new PdfExportService.PdfDocument(new byte[] {1, 2, 3}, "cv-en.pdf"));

        mvc.perform(post("/api/v1/cvs/" + cvId + "/export/pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"languageCode\":\"EN\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("cv-en.pdf")));
    }

    @Test
    void exportValidatesRequest() throws Exception {
        mvc.perform(post("/api/v1/cvs/" + cvId + "/export/pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void exportMapsMissingVersionTo404() throws Exception {
        when(pdfExportService.exportPdf(eq(cvId), any())).thenThrow(new LanguageVersionNotFoundException());

        mvc.perform(post("/api/v1/cvs/" + cvId + "/export/pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"languageCode\":\"EN\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("LANGUAGE_VERSION_NOT_FOUND"));
    }
}
