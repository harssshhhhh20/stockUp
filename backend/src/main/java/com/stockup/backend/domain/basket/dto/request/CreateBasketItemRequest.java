package com.stockup.backend.domain.basket.dto.request;

import com.stockup.backend.domain.basket.enums.BasketItemUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateBasketItemRequest(

        @NotBlank
        String productName,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal quantity,

        @NotNull
        BasketItemUnit unit,

        String brand,

        String notes

) {
}