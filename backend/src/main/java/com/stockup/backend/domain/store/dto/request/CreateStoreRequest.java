package com.stockup.backend.domain.store.dto.request;

import com.stockup.backend.domain.store.entity.enums.BusinessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateStoreRequest(

        @NotBlank
        String name,

        @NotNull
        com.stockup.backend.domain.store.entity.enums.BusinessType businessType,

        @NotBlank
        String addressLine1,

        String addressLine2,

        @NotBlank
        String city,

        @NotBlank
        String state,

        @NotBlank
        String postalCode,

        @NotBlank
        String country
) {
}