package com.cvflow.cv.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cvflow.cv.dto.response.LanguageVersionResponse;
import com.cvflow.cv.dto.response.LanguageVersionSummaryResponse;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import com.cvflow.cv.service.LanguageVersionService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LanguageVersionController.class)
@AutoConfigureMockMvc(addFilters = false)
class LanguageVersionControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private com.cvflow.auth.service.JwtService jwtService;

    @MockitoBean
    private LanguageVersionService languageVersionService;

    private final UUID cvId = UUID.randomUUID();
    private final UUID versionId = UUID.randomUUID();

    @Test
    void createReturns201() throws Exception {
        when(languageVersionService.create(eq(cvId), any())).thenReturn(sampleResponse());

        mvc.perform(post("/api/v1/cvs/" + cvId + "/languages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"languageCode\":\"EN\",\"name\":\"English\",\"templateCode\":\"MODERN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.languageCode").value("EN"));
    }

    @Test
    void listReturns200() throws Exception {
        when(languageVersionService.list(cvId)).thenReturn(List.of(sampleSummary()));

        mvc.perform(get("/api/v1/cvs/" + cvId + "/languages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getReturns200() throws Exception {
        when(languageVersionService.get(cvId, versionId)).thenReturn(sampleResponse());

        mvc.perform(get("/api/v1/cvs/" + cvId + "/languages/" + versionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateReturns200() throws Exception {
        when(languageVersionService.update(eq(cvId), eq(versionId), any())).thenReturn(sampleResponse());

        mvc.perform(put("/api/v1/cvs/" + cvId + "/languages/" + versionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"name\":\"English\",\"templateCode\":\"MINIMAL\",\"content\":{},\"sectionOrder\":[\"SUMMARY\"],\"version\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteReturns200() throws Exception {
        mvc.perform(delete("/api/v1/cvs/" + cvId + "/languages/" + versionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createValidatesRequest() throws Exception {
        mvc.perform(post("/api/v1/cvs/" + cvId + "/languages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"languageCode\":\"EN\",\"name\":\"\",\"templateCode\":\"MODERN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    private LanguageVersionResponse sampleResponse() {
        return new LanguageVersionResponse(
                versionId,
                cvId,
                LanguageCode.EN,
                "English",
                TemplateCode.MODERN,
                CvContent.empty(),
                CvSection.defaultOrder(),
                Instant.now(),
                Instant.now(),
                0L);
    }

    private LanguageVersionSummaryResponse sampleSummary() {
        return new LanguageVersionSummaryResponse(
                versionId, cvId, LanguageCode.EN, "English", TemplateCode.MODERN, Instant.now(), 0L);
    }
}
