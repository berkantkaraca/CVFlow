package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidSuggestionStateException extends ApiException {
    public InvalidSuggestionStateException() {
        super("INVALID_SUGGESTION_STATE", "Only pending suggestions can be accepted or rejected",
                HttpStatus.CONFLICT);
    }
}
