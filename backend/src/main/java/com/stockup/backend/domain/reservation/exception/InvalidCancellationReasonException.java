package com.stockup.backend.domain.reservation.exception;

public class InvalidCancellationReasonException extends RuntimeException {
    public InvalidCancellationReasonException(String message) {
        super(message);
    }
}
