package com.stockup.backend.domain.reservation.dto;

import com.stockup.backend.domain.basket.enums.BasketItemUnit;

import java.math.BigDecimal;

public record ReservedItemResponse(
        String productName,
        BigDecimal quantity,
        BasketItemUnit unit
) {
}
