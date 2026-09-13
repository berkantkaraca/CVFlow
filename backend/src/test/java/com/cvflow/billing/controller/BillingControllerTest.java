package com.cvflow.billing.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cvflow.auth.service.JwtService;
import com.cvflow.billing.dto.UsageResponse;
import com.cvflow.billing.service.UsageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BillingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BillingControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UsageService usageService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void usageReturns200() throws Exception {
        Mockito.when(usageService.usage())
                .thenReturn(new UsageResponse("FREE", 3, 50, 1, 10, "2026-09-01T00:00:00Z"));

        mvc.perform(get("/api/v1/billing/usage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.plan").value("FREE"))
                .andExpect(jsonPath("$.data.aiCallsUsed").value(3));
    }
}
