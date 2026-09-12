package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {
    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", "Invalid email or password", HttpStatus.UNAUTHORIZED);
    }
}
