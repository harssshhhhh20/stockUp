package com.stockup.backend.domain.auth.exception;

import com.stockup.backend.common.exceptions.model.BaseException;
import org.springframework.http.HttpStatus;

public class OtpRateLimitedException extends BaseException {

    public OtpRateLimitedException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
}
