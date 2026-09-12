package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidSectionOrderException extends ApiException {
    public InvalidSectionOrderException(String message) {
        super("INVALID_SECTION_ORDER", message, HttpStatus.BAD_REQUEST);
    }
}
