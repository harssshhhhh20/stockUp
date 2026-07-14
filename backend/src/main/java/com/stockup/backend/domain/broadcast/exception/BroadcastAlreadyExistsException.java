package com.stockup.backend.domain.broadcast.exception;

public class BroadcastAlreadyExistsException extends RuntimeException {
    public BroadcastAlreadyExistsException(String message) {
        super(message);
    }
}
