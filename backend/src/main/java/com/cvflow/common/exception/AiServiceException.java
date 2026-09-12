package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class AiServiceException extends ApiException {
    public AiServiceException(String code, String message) {
        super(code, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public AiServiceException(String message) {
        this("AI_SERVICE_ERROR", message);
    }
}
