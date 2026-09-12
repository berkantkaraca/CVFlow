package com.cvflow.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthSecurityTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void protectedEndpointsRequireAuthentication() throws Exception {
        mvc.perform(get("/api/v1/cvs")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.success").value(false));

        mvc.perform(get("/api/v1/cvs")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerLoginAndAuthenticatedRequest() throws Exception {
        String email = "security-test-" + System.currentTimeMillis() + "@example.com";

        MvcResult register = mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();
        String token = com.jayway.jsonpath.JsonPath.read(register.getResponse().getContentAsString(), "$.data.token");

        mvc.perform(get("/api/v1/cvs").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("EMAIL_ALREADY_EXISTS"));

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists());
    }
}
