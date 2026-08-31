package com.stockup.backend.domain.merchantoffer.dto;

import com.stockup.backend.domain.merchantoffer.enums.MerchantOfferItemStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record MerchantOfferItemSummaryResponse(

        UUID basketItemId,

        String productName,

        MerchantOfferItemStatus status,

        BigDecimal availableQuantity

) {
}
