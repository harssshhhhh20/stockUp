package com.stockup.backend.domain.user.dto;

import java.util.Set;
import java.util.UUID;

/**
 * Everything the app needs to decide where to send someone on open: whether
 * they have told us who they are, and whether they run a shop.
 *
 * Returned in one call so the client never has to guess its way through
 * onboarding with three separate requests.
 */
public record UserProfileResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phone,
        Set<String> roles,
        boolean profileComplete,
        boolean isMerchant,
        boolean hasStore,
        UUID merchantId,
        Integer bharosaScore,
        UUID storeId,
        String storeName
) {
}
