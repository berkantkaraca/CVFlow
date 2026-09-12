package com.cvflow.common.response;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        String code,
        String message,
        Map<String, String> details) {}
