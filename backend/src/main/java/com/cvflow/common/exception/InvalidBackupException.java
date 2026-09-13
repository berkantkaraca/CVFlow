package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidBackupException extends ApiException {
    public InvalidBackupException(String message) {
        super("INVALID_BACKUP", message, HttpStatus.BAD_REQUEST);
    }
}
