package com.stockup.backend.domain.auth.exception;

import com.stockup.backend.common.exceptions.model.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidOtpException extends BaseException {

    public InvalidOtpException() {
        super(
                "That code is not correct. Check it and try again.",
                HttpStatus.UNAUTHORIZED
        );
    }
}
