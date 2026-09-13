package com.cvflow.common.exception;

import org.springframework.http.HttpStatus;

public class ShareNotFoundException extends ApiException {
    public ShareNotFoundException() {
        super("SHARE_NOT_FOUND", "Shared link not found or expired", HttpStatus.NOT_FOUND);
    }
}
