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

        List<MerchantOfferItemSummaryResponse> items

) {
}
