package com.stockup.backend.domain.admin.dto;

import com.stockup.backend.domain.store.entity.enums.BusinessType;

import java.util.UUID;

public record StoreSummaryResponse(

        UUID storeId,

        String name,

        BusinessType businessType,

        String merchantEmail,

        String city

) {
}
