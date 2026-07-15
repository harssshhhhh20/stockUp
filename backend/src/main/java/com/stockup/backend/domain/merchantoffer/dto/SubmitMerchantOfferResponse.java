package com.stockup.backend.domain.merchantoffer.dto;

import java.util.UUID;

public record SubmitMerchantOfferResponse(
        UUID merchantOfferId
) {
}