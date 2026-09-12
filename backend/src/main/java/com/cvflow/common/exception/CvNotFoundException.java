package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class CvNotFoundException extends ApiException {
    public CvNotFoundException() {
        super("CV_NOT_FOUND", "CV not found", HttpStatus.NOT_FOUND);
    }
}
