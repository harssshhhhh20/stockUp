package com.stockup.backend.domain.bharosa.dto;

import com.stockup.backend.domain.bharosa.BharosaTag;

import java.util.List;
import java.util.UUID;

/**
 * What a client needs to render the badge, the banner and the explanation —
 * and nothing more. Raw pillar values stay server-side; the point is that
 * shoppers read sentences, not coefficients.
 */
public record BharosaResponse(
        UUID merchantId,
        UUID storeId,
        String storeName,
        int score,
        String band,
        boolean unproven,
        List<BharosaTag> tags,
        List<String> reasons,
        long basedOnInteractions,
        long distinctCustomers
) {
}
