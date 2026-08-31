package com.stockup.backend.domain.merchant.service;

import com.stockup.backend.domain.merchant.entity.Merchant;

public interface BharosaScoreService {

    int RESERVATION_COMPLETED_DELTA = 5;
    int MERCHANT_CANCELLED_DELTA = -10;
    int MERCHANT_NO_SHOW_DELTA = -25;

    /**
     * Merchant viewed a broadcast and submitted an offer — engaged, regardless of outcome.
     */
    int BROADCAST_RESPONDED_DELTA = 2;

    /**
     * Merchant viewed a broadcast but let it expire without responding — worse than never
     * seeing it at all, since they made an informed choice to ignore it.
     */
    int BROADCAST_VIEWED_NOT_RESPONDED_DELTA = -8;

    /**
     * Merchant never opened a broadcast before it expired.
     */
    int BROADCAST_NOT_VIEWED_DELTA = -2;

    void adjust(Merchant merchant, int delta, String reason);
}
