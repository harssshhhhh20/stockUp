package com.stockup.backend.domain.auth.exception;

import com.stockup.backend.common.exceptions.model.BaseException;
import org.springframework.http.HttpStatus;


public class InvalidTokenException extends BaseException {

    public InvalidTokenException() {
        super(
                "Invalid authentication token.",
                HttpStatus.UNAUTHORIZED

        );
    }
}