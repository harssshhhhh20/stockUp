package com.stockup.backend.domain.store.dto.response;

import java.util.UUID;

/**
 * {@code url} is a path on this API, not an absolute URL: the server can't know
 * which address the phone reached it on (LAN IP, tunnel, prod domain).
 */
public record MenuImageResponse(
        UUID imageId,
        int page,
        String url
) {
}
