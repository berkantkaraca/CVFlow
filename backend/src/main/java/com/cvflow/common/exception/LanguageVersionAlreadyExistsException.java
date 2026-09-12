package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class LanguageVersionAlreadyExistsException extends ApiException {
    public LanguageVersionAlreadyExistsException() {
        super("LANGUAGE_VERSION_ALREADY_EXISTS", "Language version already exists", HttpStatus.CONFLICT);
    }
}
