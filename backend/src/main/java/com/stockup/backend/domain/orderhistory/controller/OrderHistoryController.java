package com.stockup.backend.domain.orderhistory.controller;

import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.domain.orderhistory.dto.MerchantStatsResponse;
import com.stockup.backend.domain.orderhistory.dto.OrderDetailResponse;
import com.stockup.backend.domain.orderhistory.service.OrderHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Order history. Detail is visible only to the shopper who placed it and the
 * shop that received it — enforced in the service, never assumed of the client.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderHistoryController {

    private final OrderHistoryService orderHistoryService;

    @GetMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(
            @PathVariable UUID reservationId
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.FETCHED,
                orderHistoryService.getOrder(reservationId)
        );
    }

    /** The shop's own dashboard: their numbers, and which pillar each one feeds. */
    @GetMapping("/merchant/stats")
    public ResponseEntity<ApiResponse<MerchantStatsResponse>> merchantStats(
            @RequestParam(defaultValue = "30") int windowDays
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.FETCHED,
                orderHistoryService.merchantStats(Math.min(windowDays, 365))
        );
    }
}
