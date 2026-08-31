package com.stockup.backend.domain.broadcast.exception;

public class BroadcastRecipientNotViewedException extends RuntimeException {
    public BroadcastRecipientNotViewedException(String message) {
        super(message);
    }
}
