package com.stockup.backend.common.response;

public record ApiError(
        String field,
        String message
) {
}
