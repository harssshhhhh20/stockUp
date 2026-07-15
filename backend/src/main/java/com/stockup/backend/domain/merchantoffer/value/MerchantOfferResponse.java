package com.stockup.backend.domain.merchantoffer.value;


import com.stockup.backend.domain.basket.entity.BasketItem;
import com.stockup.backend.domain.merchantoffer.enums.MerchantOfferItemStatus;

import java.math.BigDecimal;

public record MerchantOfferResponse(
        BasketItem basketItem,
        MerchantOfferItemStatus status,
        BigDecimal availableQuantity
) {
}
