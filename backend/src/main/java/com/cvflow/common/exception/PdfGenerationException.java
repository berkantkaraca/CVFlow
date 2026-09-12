package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class PdfGenerationException extends ApiException {
    public PdfGenerationException() {
        super("PDF_GENERATION_FAILED", "CV could not be exported as PDF", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public PdfGenerationException(String message) {
        super("PDF_GENERATION_FAILED", message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
