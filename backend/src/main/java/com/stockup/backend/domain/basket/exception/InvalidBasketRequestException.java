package com.stockup.backend.domain.basket.exception;

import com.stockup.backend.common.exceptions.model.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidBasketRequestException extends BaseException {

  public InvalidBasketRequestException(String message) {
    super(message, HttpStatus.UNPROCESSABLE_ENTITY);
  }
}