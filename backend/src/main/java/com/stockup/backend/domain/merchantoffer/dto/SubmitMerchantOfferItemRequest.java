package com.stockup.backend.domain.merchantoffer.dto;

import com.stockup.backend.domain.merchantoffer.enums.MerchantOfferItemStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record SubmitMerchantOfferItemRequest(

        @NotNull
        UUID basketItemId,

        @NotNull
        MerchantOfferItemStatus status,

        BigDecimal availableQuantity,

        /**
         * What this shop charges per unit. Arrives pre-filled from the shop's
         * own price memory, so in most replies it is sent back untouched.
         */
        @DecimalMin(value = "0.01", message = "A price has to be more than zero.")
        BigDecimal quotedUnitPrice

) {
}