package com.stockup.backend.domain.basket.dto.response;

import com.stockup.backend.domain.basket.enums.BasketStatus;
import com.stockup.backend.domain.basket.enums.BasketTargetMode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record BasketHistoryResponse(

        UUID basketId,

        BasketStatus status,

        Instant createdAt,

        LocalDateTime expiresAt,

        int totalItems,

        BasketTargetMode targetMode

) {
}