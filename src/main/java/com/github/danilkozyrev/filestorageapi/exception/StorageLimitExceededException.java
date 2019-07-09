package com.github.danilkozyrev.filestorageapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INSUFFICIENT_STORAGE)
public class StorageLimitExceededException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "You have exceeded your storage limit";

    public StorageLimitExceededException() {
        super(DEFAULT_MESSAGE);
    }

    public StorageLimitExceededException(String message) {
        super(message);
    }

}
