package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidFieldException extends ApiException {
    public InvalidFieldException(String message) {
        super("INVALID_FIELD", message, HttpStatus.BAD_REQUEST);
    }
}
