package com.stockup.backend.domain.basket.dto.response;

import com.stockup.backend.domain.basket.enums.BasketStatus;
import com.stockup.backend.domain.basket.enums.BasketTargetMode;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BasketDetailsResponse(

        UUID basketId,

        BasketStatus status,

        BasketTargetMode targetMode,

        Integer searchRadius,

        BigDecimal basketLatitude,

        BigDecimal basketLongitude,

        Instant createdAt,

        Instant expiresAt,

        List<BasketItemResponse> items

) {
}