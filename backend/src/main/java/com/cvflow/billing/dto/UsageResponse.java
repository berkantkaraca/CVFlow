package com.cvflow.billing.dto;

public record UsageResponse(
        String plan, long aiCallsUsed, int aiCallsLimit, long cvsUsed, int cvsMax, String periodStart) {}
