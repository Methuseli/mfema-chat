package com.mfemachat.chatapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictingRequestException extends RuntimeException{
    public ConflictingRequestException(String message) {
        super(message);
    }

    public ConflictingRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
