package com.stockup.backend.common.exceptions.handler;

import com.stockup.backend.common.exceptions.model.BaseException;
import com.stockup.backend.common.response.ApiError;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
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
}