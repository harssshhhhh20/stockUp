package com.stockup.backend.domain.store.dto.response;

public record MenuImageContent(
        String contentType,
        byte[] data
) {
}
