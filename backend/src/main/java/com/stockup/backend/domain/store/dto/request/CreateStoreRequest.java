package com.stockup.backend.domain.store.dto.request;

import com.stockup.backend.domain.store.entity.enums.BusinessType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateStoreRequest(

        @NotBlank
        String name,

        @NotNull
        BusinessType businessType,

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
        String country,

        // Both null when the shopkeeper typed their address instead of sharing
        // their location; the server then looks the address up.
        @DecimalMin("-90.0") @DecimalMax("90.0")
        Double latitude,

        @DecimalMin("-180.0") @DecimalMax("180.0")
        Double longitude
) {
}