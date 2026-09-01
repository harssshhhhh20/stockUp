package com.stockup.backend.domain.store.dto.response;

import com.stockup.backend.domain.store.entity.enums.BusinessType;

import java.util.UUID;

public record StoreResponse(
        UUID storeId,
        String name,
        BusinessType businessType,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country,
        Double latitude,
        Double longitude
) {
}
