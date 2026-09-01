package com.stockup.backend.domain.merchant.dto.response;

import java.util.UUID;

public record MerchantProfileResponse(
        UUID merchantId,
        int bharosaScore
) {
}
