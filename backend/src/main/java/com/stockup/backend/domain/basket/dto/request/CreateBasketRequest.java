package com.stockup.backend.domain.basket.dto.request;

import com.stockup.backend.domain.basket.enums.BasketTargetMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateBasketRequest(

        @NotNull
        BasketTargetMode targetMode,

        Integer searchRadiusMeters,

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        BigDecimal basketLatitude,

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        BigDecimal basketLongitude,

        @Valid
        @NotEmpty
        List<CreateBasketItemRequest> items,

        List<UUID> storeIds

) {
}