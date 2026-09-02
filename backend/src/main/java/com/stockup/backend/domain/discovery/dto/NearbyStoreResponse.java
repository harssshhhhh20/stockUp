package com.stockup.backend.domain.discovery.dto;

import com.stockup.backend.domain.bharosa.BharosaTag;
import com.stockup.backend.domain.store.entity.enums.BusinessType;

import java.util.List;
import java.util.UUID;

/**
 * A store as it appears in discovery: enough to choose by, and nothing about
 * anyone's private order history.
 */
public record NearbyStoreResponse(
        UUID storeId,
        String name,
        BusinessType businessType,
        String city,
        double distanceKm,
        int bharosa,
        String band,
        List<BharosaTag> tags,
        /** Why this store is placed here. Debug/transparency, not shown by default. */
        double rankScore
) {
}
