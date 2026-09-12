package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class LanguageVersionNotFoundException extends ApiException {
    public LanguageVersionNotFoundException() {
        super("LANGUAGE_VERSION_NOT_FOUND", "Language version not found", HttpStatus.NOT_FOUND);
    }
}
