package com.stockup.backend.domain.basket.exception;

public class ActiveBasketAlreadyExistsException extends RuntimeException {
    public ActiveBasketAlreadyExistsException(String message) {
        super(message);
    }
}
