package com.stockup.backend.domain.orderhistory.dto;

/**
 * The merchant dashboard. Every figure names the Bharosa pillar it feeds, so
 * the dashboard doubles as the explanation of their score — a shopkeeper should
 * never have to guess which behaviour to change.
 */
public record MerchantStatsResponse(
        long ordersCompleted,
        Long averageResponseSeconds,
        Double answeredRate,
        Double completionRate,
        Double cancellationRate,
        Double repeatCustomerRate,
        long distinctCustomers,
        int bharosaScore
) {
}
