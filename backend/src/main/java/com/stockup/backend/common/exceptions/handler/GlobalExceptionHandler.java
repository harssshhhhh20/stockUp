package com.stockup.backend.common.exceptions.handler;

import com.stockup.backend.common.exceptions.model.BaseException;
import com.stockup.backend.common.response.ApiError;
import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.domain.merchant.exception.MerchantAlreadyExistsException;
import com.stockup.backend.infrastructure.notification.email.exception.EmailDeliveryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<?> handleBaseException(BaseException exception) {

        return ApiResponseFactory.failure(
                exception.getStatus(),
                ResponseMessage.FAILURE,
                List.of(new ApiError(null, exception.getMessage()))
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException exception) {

        List<ApiError> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ApiError(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        return ApiResponseFactory.failure(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                ResponseMessage.VALIDATION_FAILED,
                errors
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpectedException(Exception exception) {

        return ApiResponseFactory.failure(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                ResponseMessage.INTERNAL_SERVER_ERROR,
                List.of(new ApiError(null, "Unexpected error occurred."))
        );
    }

    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmailDeliveryException(
            EmailDeliveryException ex
    ) {
        return ApiResponseFactory.failure(
                HttpStatus.SERVICE_UNAVAILABLE,
                ResponseMessage.EMAIL_DELIVERY_FAILED,
                List.of(
                        new ApiError(
                                null,
                                "Unable to send verification email. Please try again later."
                        )
                )
        );
    }

    @ExceptionHandler(MerchantAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleMerchantAlreadyExists(
            MerchantAlreadyExistsException ex
    ) {
        return ApiResponseFactory.failure(
                HttpStatus.CONFLICT,
                ResponseMessage.MERCHANT_ALREADY_EXISTS,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }
}