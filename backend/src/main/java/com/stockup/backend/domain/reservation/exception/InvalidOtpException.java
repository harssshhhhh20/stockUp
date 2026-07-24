package com.stockup.backend.domain.reservation.exception;

public class InvalidOtpException extends RuntimeException {
  public InvalidOtpException(String message) {
    super(message);
  }
}
