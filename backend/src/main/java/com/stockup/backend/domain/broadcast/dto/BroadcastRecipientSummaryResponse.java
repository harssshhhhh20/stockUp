package com.stockup.backend.domain.broadcast.dto;

import com.stockup.backend.domain.basket.enums.BasketItemUnit;
import com.stockup.backend.domain.broadcast.entity.enums.BroadcastRecipientStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BroadcastRecipientSummaryResponse(

        UUID broadcastRecipientId,

        UUID basketId,

        BroadcastRecipientStatus status,

        Instant createdAt,

        Instant viewedAt,

        Instant basketExpiresAt,

        List<Item> items

) {
    public record Item(
            UUID basketItemId,
            String productName,
            BigDecimal quantity,
            BasketItemUnit unit,
            String brand,
            String notes
    ) {
    }
}
