package com.stockup.backend.domain.feedback.exception;

import com.stockup.backend.common.exceptions.model.BaseException;
import org.springframework.http.HttpStatus;

public class FeedbackAlreadyGivenException extends BaseException {
    public FeedbackAlreadyGivenException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
