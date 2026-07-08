package com.stockup.backend.common.response;

import java.time.Instant;
import java.util.List;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        List<ApiError> errors,
        Instant timestamp
) {
}
