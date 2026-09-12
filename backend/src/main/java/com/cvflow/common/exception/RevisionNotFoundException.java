package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class RevisionNotFoundException extends ApiException {
    public RevisionNotFoundException() {
        super("REVISION_NOT_FOUND", "Content revision not found", HttpStatus.NOT_FOUND);
    }
}
