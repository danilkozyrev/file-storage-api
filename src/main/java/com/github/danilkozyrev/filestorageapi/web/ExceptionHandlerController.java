package com.github.danilkozyrev.filestorageapi.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Access is denied or the requested resource does not exist")
    public void handleAccessDeniedException() {
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(code = HttpStatus.PAYLOAD_TOO_LARGE, reason = "The request exceeds its maximum permitted size")
    public void handleMaxUploadSizeExceededException() {
    }

}
