package com.stockup.backend.domain.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record MerchantSummaryResponse(

        UUID merchantId,

        String email,

        int bharosaScore,

        Instant createdAt

) {
}
