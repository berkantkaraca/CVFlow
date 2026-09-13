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

import com.cvflow.auth.service.JwtService;
import com.cvflow.cv.dto.response.CoverLetterResponse;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.service.CoverLetterService;
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

@WebMvcTest(CoverLetterController.class)
@AutoConfigureMockMvc(addFilters = false)
class CoverLetterControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CoverLetterService coverLetterService;

    @MockitoBean
    private JwtService jwtService;

    private final UUID cvId = UUID.randomUUID();

    @Test
    void createReturns201() throws Exception {
        when(coverLetterService.create(eq(cvId), any())).thenReturn(sample());

        mvc.perform(post("/api/v1/cvs/" + cvId + "/cover-letters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"languageCode\":\"EN\",\"title\":\"Application\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void listReturns200() throws Exception {
        when(coverLetterService.list(cvId)).thenReturn(List.of());

        mvc.perform(get("/api/v1/cvs/" + cvId + "/cover-letters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateReturns200() throws Exception {
        UUID letterId = UUID.randomUUID();
        when(coverLetterService.update(eq(cvId), eq(letterId), any())).thenReturn(sample());

        mvc.perform(put("/api/v1/cvs/" + cvId + "/cover-letters/" + letterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"T\",\"body\":\"b\",\"version\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteReturns200() throws Exception {
        mvc.perform(delete("/api/v1/cvs/" + cvId + "/cover-letters/" + UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private CoverLetterResponse sample() {
        return new CoverLetterResponse(
                UUID.randomUUID(),
                cvId,
                LanguageCode.EN,
                "Application",
                null,
                "Acme",
                "Dev",
                "Body",
                Instant.now(),
                Instant.now(),
                0L);
    }
}
