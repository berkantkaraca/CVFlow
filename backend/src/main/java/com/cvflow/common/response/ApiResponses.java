package com.cvflow.common.response;

import java.time.Instant;
import java.util.Map;

public final class ApiResponses {
    private ApiResponses() {}

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, Instant.now());
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(String code, String message, Map<String, String> details) {
        return new ApiResponse<>(false, null, new ApiError(code, message, details), Instant.now());
    }
}
