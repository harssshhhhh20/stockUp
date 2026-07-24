package com.stockup.backend.domain.reservation.exception;

public class MerchantOfferNotFoundException extends RuntimeException {
    public MerchantOfferNotFoundException(String message) {
        super(message);
    }
}
