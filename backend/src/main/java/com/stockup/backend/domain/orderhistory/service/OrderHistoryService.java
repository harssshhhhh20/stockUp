package com.stockup.backend.domain.orderhistory.service;

import com.stockup.backend.domain.orderhistory.dto.MerchantStatsResponse;
import com.stockup.backend.domain.orderhistory.dto.OrderDetailResponse;

import java.util.UUID;

public interface OrderHistoryService {

    OrderDetailResponse getOrder(UUID reservationId);

    MerchantStatsResponse merchantStats(int windowDays);
}
