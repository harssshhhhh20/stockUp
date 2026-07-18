package com.stockup.backend.domain.reservation.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
