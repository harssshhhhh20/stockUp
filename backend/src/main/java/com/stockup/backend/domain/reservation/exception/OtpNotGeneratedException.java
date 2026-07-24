package com.stockup.backend.domain.reservation.exception;

public class OtpNotGeneratedException extends RuntimeException {
  public OtpNotGeneratedException(String message) {
    super(message);
  }
}
