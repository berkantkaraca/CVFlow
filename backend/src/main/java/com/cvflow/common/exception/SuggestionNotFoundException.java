package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class SuggestionNotFoundException extends ApiException {
    public SuggestionNotFoundException() {
        super("SUGGESTION_NOT_FOUND", "AI suggestion not found", HttpStatus.NOT_FOUND);
    }
}
