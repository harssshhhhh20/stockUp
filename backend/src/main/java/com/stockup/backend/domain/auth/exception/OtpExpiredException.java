package com.stockup.backend.domain.auth.exception;

import com.stockup.backend.common.exceptions.model.BaseException;
import org.springframework.http.HttpStatus;

public class OtpExpiredException extends BaseException {

    public OtpExpiredException() {
        super(
                "That code has expired. Request a new one.",
                HttpStatus.UNAUTHORIZED
        );
    }
}
