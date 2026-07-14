package com.stockup.backend.domain.basket.exception;

public class InvalidBasketRequestException extends RuntimeException {

  public InvalidBasketRequestException(String message) {
    super(message);
  }
}