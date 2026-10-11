package com.stockup.backend.domain.merchantoffer.dto;

import com.stockup.backend.domain.merchantoffer.enums.MerchantOfferStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MerchantOfferSummaryResponse(

        UUID merchantOfferId,

        UUID broadcastRecipientId,

        UUID storeId,

        String storeName,

        MerchantOfferStatus status,

        Instant submittedAt,

        List<MerchantOfferItemSummaryResponse> items,

        /**
         * What the shopper expected this list to cost — their own numbers, the
         * figure the counter animates down FROM. Null if they priced nothing.
         */
        java.math.BigDecimal expectedTotal,

        /** What this shop will actually charge. Null if it didn't quote. */
        java.math.BigDecimal quotedTotal,

        /**
         * expectedTotal − quotedTotal when the shop came in under. Null when
         * either number is missing, and negative is deliberately kept: a shop
         * quoting above expectation should not be dressed up as a saving.
         */
        java.math.BigDecimal saving

) {
}
