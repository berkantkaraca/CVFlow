package com.cvflow.pdf.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cvflow.auth.service.JwtService;
import com.cvflow.pdf.service.DocxExportService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DocxController.class)
@AutoConfigureMockMvc(addFilters = false)
class DocxControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private DocxExportService docxExportService;

    @MockitoBean
    private JwtService jwtService;

    private final UUID cvId = UUID.randomUUID();

    @Test
    void exportReturnsDocxAttachment() throws Exception {
        when(docxExportService.exportDocx(eq(cvId), any()))
                .thenReturn(new DocxExportService.DocxDocument(new byte[] {1, 2, 3}, "cv-en.docx"));

        mvc.perform(post("/api/v1/cvs/" + cvId + "/export/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"languageCode\":\"EN\"}"))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .contentType(
                                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .andExpect(
                        header()
                                .string(
                                        HttpHeaders.CONTENT_DISPOSITION,
                                        org.hamcrest.Matchers.containsString("cv-en.docx")));
    }

    @Test
    void exportValidatesRequest() throws Exception {
        mvc.perform(post("/api/v1/cvs/" + cvId + "/export/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}
