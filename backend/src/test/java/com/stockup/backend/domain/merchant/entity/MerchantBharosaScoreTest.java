package com.stockup.backend.domain.merchant.entity;

import com.stockup.backend.domain.merchant.service.BharosaScoreService;
import com.stockup.backend.domain.user.entity.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantBharosaScoreTest {

    private Merchant newMerchant() {
        return new Merchant(new User("shop@example.com"));
    }

    @Test
    void startsAtFullTrust() {
        assertThat(newMerchant().getBharosaScore()).isEqualTo(100);
    }

    @Test
    void neverExceedsOneHundred() {
        Merchant merchant = newMerchant();

        merchant.adjustBharosaScore(BharosaScoreService.RESERVATION_COMPLETED_DELTA);

        assertThat(merchant.getBharosaScore()).isEqualTo(100);
    }

    @Test
    void neverFallsBelowZero() {
        Merchant merchant = newMerchant();

        for (int i = 0; i < 10; i++) {
            merchant.adjustBharosaScore(BharosaScoreService.MERCHANT_NO_SHOW_DELTA);
        }

        assertThat(merchant.getBharosaScore()).isZero();
    }

    @Test
    void ignoringAViewedRequestCostsMoreThanNeverSeeingIt() {
        Merchant ignored = newMerchant();
        Merchant unseen = newMerchant();

        ignored.adjustBharosaScore(BharosaScoreService.BROADCAST_VIEWED_NOT_RESPONDED_DELTA);
        unseen.adjustBharosaScore(BharosaScoreService.BROADCAST_NOT_VIEWED_DELTA);

        assertThat(ignored.getBharosaScore()).isLessThan(unseen.getBharosaScore());
    }

    @Test
    void respondingIsRewardedAndCancellingIsPenalised() {
        Merchant merchant = newMerchant();
        merchant.adjustBharosaScore(-50); // start mid-range so both directions move

        int base = merchant.getBharosaScore();

        merchant.adjustBharosaScore(BharosaScoreService.BROADCAST_RESPONDED_DELTA);
        assertThat(merchant.getBharosaScore()).isGreaterThan(base);

        merchant.adjustBharosaScore(BharosaScoreService.MERCHANT_CANCELLED_DELTA);
        assertThat(merchant.getBharosaScore()).isLessThan(base);
    }
}
