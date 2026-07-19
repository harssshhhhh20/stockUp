package com.stockup.backend.domain.basket.exception;

import com.stockup.backend.common.exceptions.model.BaseException;
import org.springframework.http.HttpStatus;

public class PendingBroadcastAlreadyExistsException extends BaseException {
    public PendingBroadcastAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
