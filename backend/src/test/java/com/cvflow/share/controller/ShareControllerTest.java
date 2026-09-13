package com.cvflow.share.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cvflow.auth.service.JwtService;
import com.cvflow.share.dto.ShareDtos.ShareResponse;
import com.cvflow.share.service.ShareService;
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

@WebMvcTest(ShareController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShareControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ShareService shareService;

    @MockitoBean
    private JwtService jwtService;

    private final UUID cvId = UUID.randomUUID();
    private final UUID versionId = UUID.randomUUID();

    @Test
    void createReturns201() throws Exception {
        when(shareService.create(eq(cvId), eq(versionId), any()))
                .thenReturn(new ShareResponse(UUID.randomUUID(), "token", null, Instant.now()));

        mvc.perform(post("/api/v1/cvs/" + cvId + "/languages/" + versionId + "/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.token").value("token"));
    }

    @Test
    void listReturns200() throws Exception {
        when(shareService.list(cvId, versionId)).thenReturn(List.of());

        mvc.perform(get("/api/v1/cvs/" + cvId + "/languages/" + versionId + "/share"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void revokeReturns200() throws Exception {
        mvc.perform(delete("/api/v1/cvs/" + cvId + "/languages/" + versionId + "/share/" + UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
