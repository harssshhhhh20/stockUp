package com.stockup.backend.domain.feedback.exception;

import com.stockup.backend.common.exceptions.model.BaseException;
import org.springframework.http.HttpStatus;

public class FeedbackNotAllowedException extends BaseException {
    public FeedbackNotAllowedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
