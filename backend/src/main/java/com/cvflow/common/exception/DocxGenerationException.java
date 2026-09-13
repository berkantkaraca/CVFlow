package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class DocxGenerationException extends ApiException {
    public DocxGenerationException(String message) {
        super("DOCX_GENERATION_FAILED", message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
