
package com.stockup.backend.common.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

public final class ApiResponseFactory {

    private ApiResponseFactory() {}

    public static <T> ResponseEntity<ApiResponse<T>> success(
            ResponseMessage message,
            T data
    ) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        message.getMessage(),
                        data,
                        List.of(),
                        Instant.now()
                )
        );
    }

    public static ResponseEntity<ApiResponse<Object>> failure(
            HttpStatus status,
            ResponseMessage message,
            List<ApiError> errors
    ) {

        return ResponseEntity.status(status)
                .body(
                        new ApiResponse<>(
                                false,
                                message.getMessage(),
                                null,
                                errors,
                                Instant.now()
                        )
                );
    }
}