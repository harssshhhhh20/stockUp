package com.stockup.backend.domain.reservation.exception;

public class MerchantCancellationWindowClosedException extends RuntimeException {
    public MerchantCancellationWindowClosedException(String message) {
        super(message);
    }
}
