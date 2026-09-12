package com.cvflow.cv.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cvflow.cv.dto.response.CvResponse;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.service.CvService;
import tools.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CvController.class)
class CvControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CvService cvService;

    @Test
    void createReturns201() throws Exception {
        CvResponse response = sampleResponse();
        when(cvService.create(any())).thenReturn(response);

        mvc.perform(post("/api/v1/cvs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Backend CV\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Backend CV"));
    }

    @Test
    void createRejectsBlankName() throws Exception {
        mvc.perform(post("/api/v1/cvs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void getReturns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(cvService.get(id)).thenReturn(sampleResponse());

        mvc.perform(get("/api/v1/cvs/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateSendsVersion() throws Exception {
        UUID id = UUID.randomUUID();
        when(cvService.update(eq(id), any())).thenReturn(sampleResponse());
        String body = objectMapper.writeValueAsString(
                new UpdateBody("Backend CV", CvContent.empty(), CvSection.defaultOrder(), 5L));

        mvc.perform(put("/api/v1/cvs/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private CvResponse sampleResponse() {
        return new CvResponse(
                UUID.randomUUID(),
                "Backend CV",
                CvContent.empty(),
                CvSection.defaultOrder(),
                Instant.now(),
                Instant.now(),
                0L);
    }

    private record UpdateBody(
            String name, CvContent content, List<CvSection> sectionOrder, Long version) {}
}
