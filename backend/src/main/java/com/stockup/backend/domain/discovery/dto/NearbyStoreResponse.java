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
        /** One human line on why this shop is worth asking. Derived, never typed. */
        String knownFor,
        Double averageStars,
        long reviewCount,
        /** Why this store is placed here. Debug/transparency, not shown by default. */
        double rankScore
) {
}
