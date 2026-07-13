package com.stockup.backend.domain.store.exception;

public class StoreAlreadyExistsException extends RuntimeException {
    public StoreAlreadyExistsException() {
        super("Merchant already owns a store.");
    }
}
