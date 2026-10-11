package com.stockup.backend.domain.merchantoffer.dto;

import com.stockup.backend.domain.merchantoffer.enums.MerchantOfferItemStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record MerchantOfferItemSummaryResponse(

        UUID basketItemId,

        String productName,

        MerchantOfferItemStatus status,

        BigDecimal availableQuantity,

        /** This shop's price per unit for this line. Null if it didn't quote. */
        BigDecimal quotedUnitPrice,

        /** What the shopper said they expected to pay. Null if they didn't say. */
        BigDecimal expectedUnitPrice

) {
}
