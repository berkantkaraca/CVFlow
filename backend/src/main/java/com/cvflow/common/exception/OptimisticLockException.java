package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class OptimisticLockException extends ApiException {
    public OptimisticLockException() {
        super("VERSION_CONFLICT", "Resource was modified by another request. Please refresh and try again.",
                HttpStatus.CONFLICT);
    }
}
