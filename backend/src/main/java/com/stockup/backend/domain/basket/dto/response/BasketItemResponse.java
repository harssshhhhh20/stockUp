package com.stockup.backend.domain.basket.dto.response;

import com.stockup.backend.domain.basket.enums.BasketItemUnit;

import java.math.BigDecimal;
import java.util.UUID;

public record BasketItemResponse(

        UUID basketItemId,

        String productName,

        BigDecimal quantity,

        BasketItemUnit unit,

        String brand,

        String notes

) {
}