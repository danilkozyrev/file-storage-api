package com.github.danilkozyrev.filestorageapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class CircularFolderStructureException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "A circular reference in the folder tree occurs";

    public CircularFolderStructureException() {
        super(DEFAULT_MESSAGE);
    }

    public CircularFolderStructureException(String message) {
        super(message);
    }

}
