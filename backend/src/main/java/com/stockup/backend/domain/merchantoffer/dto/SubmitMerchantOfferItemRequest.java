package com.stockup.backend.domain.merchantoffer.dto;

import com.stockup.backend.domain.merchantoffer.enums.MerchantOfferItemStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record SubmitMerchantOfferItemRequest(

        @NotNull
        UUID basketItemId,

        @NotNull
        MerchantOfferItemStatus status,

        BigDecimal availableQuantity

) {
}