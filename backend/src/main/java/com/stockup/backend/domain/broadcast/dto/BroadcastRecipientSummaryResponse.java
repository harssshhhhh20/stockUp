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

        List<Item> items,

        /**
         * The sum of the per-item suggestions below — this shop's opening quote,
         * already filled in. Null when nothing could be suggested at all.
         */
        BigDecimal suggestedTotal,

        /**
         * How many orders this shopper has collected from THIS shop before.
         *
         * The whole point of relationship pricing: the shopkeeper recognises a
         * regular and decides, himself, to come in lower. Scoped to this shop —
         * a regular elsewhere is a stranger here.
         */
        int ordersWithThisShop

) {
    public record Item(
            UUID basketItemId,
            String productName,
            BigDecimal quantity,
            BasketItemUnit unit,
            String brand,
            String notes,

            /** What the shopper expects to pay per unit. Null if they didn't say. */
            BigDecimal expectedUnitPrice,

            /**
             * The price this line arrives pre-filled with, so the shopkeeper
             * corrects rather than types. Null only when nothing is known.
             */
            BigDecimal suggestedUnitPrice,

            /**
             * Where that number came from — SHOP_MEMORY, SHOPPER_ESTIMATE or
             * NONE. The app says so out loud: "your price" reads very
             * differently from "what they expect to pay".
             */
            String suggestedPriceSource
    ) {
    }
}
