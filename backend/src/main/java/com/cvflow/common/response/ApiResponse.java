package com.cvflow.common.response;

import java.time.Instant;

public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error,
        Instant timestamp) {}
