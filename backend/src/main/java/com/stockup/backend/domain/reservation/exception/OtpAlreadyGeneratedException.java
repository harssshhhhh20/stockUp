package com.stockup.backend.domain.reservation.exception;

public class OtpAlreadyGeneratedException extends RuntimeException {
  public OtpAlreadyGeneratedException(String message) {
    super(message);
  }
}
