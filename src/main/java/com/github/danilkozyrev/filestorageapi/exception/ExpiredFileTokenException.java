package com.github.danilkozyrev.filestorageapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class ExpiredFileTokenException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "The provided file token has expired";

    public ExpiredFileTokenException() {
        super(DEFAULT_MESSAGE);
    }

    public ExpiredFileTokenException(String message) {
        super(message);
    }

}
