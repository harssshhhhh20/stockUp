package com.stockup.backend.domain.discovery;

import com.stockup.backend.domain.bharosa.BharosaWeights;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ranking must answer a different question from scoring: not "is this shop
 * good" but "is this the right shop for this request". These lock in the
 * trade-off rather than the arithmetic.
 */
class RankingServiceTest {

    private final RankingService ranking = new RankingService(new BharosaWeights());

    private double score(int bharosa, double km) {
        return ranking.score(new RankingService.Candidate(null, bharosa, km, 1.0));
    }

    @Test
    void aTrustedShopSlightlyFurtherBeatsANearbyUnreliableOne() {
        double trustedAt2km = score(85, 2.0);
        double unreliableAt1km = score(60, 1.0);

        assertThat(trustedAt2km).isGreaterThan(unreliableAt1km);
    }

    @Test
    void butNobodyWalksSixKilometresForGroceries() {
        double excellentButFar = score(95, 6.0);
        double goodAndClose = score(85, 2.0);

        assertThat(goodAndClose).isGreaterThan(excellentButFar);
    }

    @Test
    void amongEquallyTrustedShopsTheNearerOneWins() {
        assertThat(score(80, 1.0)).isGreaterThan(score(80, 3.0));
    }

    @Test
    void amongEquallyCloseShopsTheMoreTrustedOneWins() {
        assertThat(score(90, 2.0)).isGreaterThan(score(50, 2.0));
    }

    @Test
    void distanceCannotFullyRescueAShopNobodyTrusts() {
        // Right next door, but it cancels on half its customers.
        double untrustedNextDoor = score(20, 0.1);
        double trustedShortWalk = score(88, 1.5);

        assertThat(trustedShortWalk).isGreaterThan(untrustedNextDoor);
    }

    @Test
    void rankingIsSeparateFromScoring() {
        // The same Bharosa produces different placements depending on the
        // request context — which is the whole reason the two are not one
        // number.
        assertThat(score(80, 0.5)).isNotEqualTo(score(80, 4.0));
    }
}
