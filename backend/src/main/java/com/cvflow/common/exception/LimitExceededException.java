package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class LimitExceededException extends ApiException {
    public LimitExceededException(String message) {
        super("LIMIT_EXCEEDED", message, HttpStatus.TOO_MANY_REQUESTS);
    }
}
