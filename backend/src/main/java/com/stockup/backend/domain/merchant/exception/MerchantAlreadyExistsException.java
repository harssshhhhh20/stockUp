package com.stockup.backend.domain.merchant.exception;

public class MerchantAlreadyExistsException extends RuntimeException {

    public MerchantAlreadyExistsException() {
        super("User is already registered as a merchant.");
    }
}