package com.stockup.backend.domain.merchantoffer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

import java.util.List;
import java.util.UUID;

public record SubmitMerchantOfferRequest(

        @NotNull
        UUID broadcastRecipientId,

        @NotEmpty
        List<@Valid SubmitMerchantOfferItemRequest> responses,

        /**
         * What the shop will charge for the whole basket. Pre-filled from the
         * shopper's own expected total and edited by the shopkeeper — usually
         * downward, for someone they know. Optional: a shop that only wants to
         * answer availability still can.
         */
        @DecimalMin(value = "0.00", message = "A total can't be negative.")
        BigDecimal quotedTotal

) {
}