package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends ApiException {
    public AccessDeniedException() {
        super("ACCESS_DENIED", "Access denied", HttpStatus.FORBIDDEN);
    }
}
