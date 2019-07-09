package com.github.danilkozyrev.filestorageapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecordNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "The requested record has not been found";

    public RecordNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public RecordNotFoundException(String message) {
        super(message);
    }

    public RecordNotFoundException(Class<?> recordClass) {
        super("The requested " + recordClass.getSimpleName() + " has not been found");
    }

    public RecordNotFoundException(Class<?> recordClass, Serializable resourceId) {
        super("The requested " + recordClass.getSimpleName() + " with id " + resourceId + " has not been found");
    }

}
