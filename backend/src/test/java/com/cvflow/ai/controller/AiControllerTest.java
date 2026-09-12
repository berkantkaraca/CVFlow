package com.cvflow.ai.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cvflow.ai.dto.AiSuggestionResponse;
import com.cvflow.ai.model.AiOperation;
import com.cvflow.ai.model.AiSuggestionPayload;
import com.cvflow.ai.model.AiSuggestionStatus;
import com.cvflow.ai.service.AiService;
import com.cvflow.cv.model.CvSection;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AiController.class)
class AiControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AiService aiService;

    private final UUID versionId = UUID.randomUUID();
    private final UUID suggestionId = UUID.randomUUID();

    @Test
    void improveReturns201() throws Exception {
        when(aiService.improve(any())).thenReturn(sample());

        mvc.perform(post("/api/v1/ai/improve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"languageVersionId\":\"" + versionId + "\",\"section\":\"SUMMARY\","
                                        + "\"field\":\"summary\",\"content\":\"old\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.operation").value("IMPROVE"));
    }

    @Test
    void improveValidatesRequest() throws Exception {
        mvc.perform(post("/api/v1/ai/improve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"languageVersionId\":\"" + versionId + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void listReturns200() throws Exception {
        when(aiService.list(eq(versionId), eq(null))).thenReturn(List.of(sample()));

        mvc.perform(get("/api/v1/ai/suggestions").param("languageVersionId", versionId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void acceptReturns200() throws Exception {
        when(aiService.accept(suggestionId)).thenReturn(accepted());

        mvc.perform(post("/api/v1/ai/suggestions/" + suggestionId + "/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    void rejectReturns200() throws Exception {
        AiSuggestionResponse rejected = new AiSuggestionResponse(
                suggestionId,
                AiOperation.IMPROVE,
                AiSuggestionStatus.REJECTED,
                new AiSuggestionPayload(CvSection.SUMMARY, null, "summary", "old", "new"),
                Instant.now());

        when(aiService.reject(suggestionId)).thenReturn(rejected);

        mvc.perform(post("/api/v1/ai/suggestions/" + suggestionId + "/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    private AiSuggestionResponse sample() {
        return new AiSuggestionResponse(
                suggestionId,
                AiOperation.IMPROVE,
                AiSuggestionStatus.PENDING,
                new AiSuggestionPayload(CvSection.SUMMARY, null, "summary", "old", "new"),
                Instant.now());
    }

    private AiSuggestionResponse accepted() {
        return new AiSuggestionResponse(
                suggestionId,
                AiOperation.IMPROVE,
                AiSuggestionStatus.ACCEPTED,
                new AiSuggestionPayload(CvSection.SUMMARY, null, "summary", "old", "new"),
                Instant.now());
    }
}
