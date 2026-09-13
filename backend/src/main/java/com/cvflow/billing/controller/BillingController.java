package com.cvflow.billing.controller;

import com.cvflow.billing.dto.UsageResponse;
import com.cvflow.billing.service.UsageService;
import com.cvflow.common.response.ApiResponse;
import com.cvflow.common.response.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {
    private final UsageService usageService;

    @GetMapping("/usage")
    public ResponseEntity<ApiResponse<UsageResponse>> usage() {
        return ResponseEntity.ok(ApiResponses.success(usageService.usage()));
    }
}
