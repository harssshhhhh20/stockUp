package com.stockup.backend.domain.merchantoffer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SubmitMerchantOfferRequest(

        @NotNull
        UUID broadcastRecipientId,

        @NotEmpty
        List<@Valid SubmitMerchantOfferItemRequest> responses

) {
}