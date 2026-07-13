package com.stockup.backend.domain.merchant.exception;

public class MerchantNotFoundException extends RuntimeException {

    public MerchantNotFoundException() {
        super("Merchant profile not found.");
    }
}
