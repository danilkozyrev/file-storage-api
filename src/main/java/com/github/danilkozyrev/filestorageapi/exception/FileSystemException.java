package com.github.danilkozyrev.filestorageapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FileSystemException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "A file system exception has occurred";

    public FileSystemException() {
        super(DEFAULT_MESSAGE);
    }

    public FileSystemException(String message) {
        super(message);
    }

    public FileSystemException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause);
    }

    public FileSystemException(String message, Throwable cause) {
        super(message, cause);
    }

}
