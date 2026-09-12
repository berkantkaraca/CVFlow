package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthenticatedException extends ApiException {
    public UnauthenticatedException() {
        super("UNAUTHENTICATED", "Authentication required", HttpStatus.UNAUTHORIZED);
    }
}
