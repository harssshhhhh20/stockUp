package com.stockup.backend.domain.broadcast.exception;

public class NoTargetStoresFoundException extends RuntimeException {
    public NoTargetStoresFoundException(String message) {
        super(message);
    }
}
