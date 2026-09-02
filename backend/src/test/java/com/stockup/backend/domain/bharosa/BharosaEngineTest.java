package com.stockup.backend.domain.bharosa;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * These lock in the properties that make Bharosa different from a star rating.
 * If one of these breaks, the model has quietly become a weighted average.
 */
class BharosaEngineTest {

    private final BharosaWeights weights = new BharosaWeights();
    private final BharosaEngine engine = new BharosaEngine(null, null, weights);

    private BharosaPillars pillars(double responsiveness, double promise,
                                   double consistency, double confidence, double trajectory) {
        return new BharosaPillars(responsiveness, promise, consistency, confidence,
                trajectory, null, 0, 0);
    }

    @Test
    void answeringFastCannotCompensateForNeverDelivering() {
        // The signature failure an arithmetic mean would forgive.
        int harmonic = engine.score(pillars(0.95, 0.20, 1.0, 50, 0));
        int wouldBeArithmetic = (int) Math.round(((0.95 + 0.20) / 2) * 100);

        assertThat(harmonic).isLessThan(wouldBeArithmetic - 15);
    }

    @Test
    void aNewShopStartsNeutralRatherThanPunished() {
        int score = engine.score(pillars(0, 0, 1.0, 0, 0));

        // Lands on the prior: unproven, not condemned.
        assertThat(score).isEqualTo(62);
    }

    @Test
    void thinEvidenceIsPulledTowardThePrior() {
        // Perfect behaviour, but only one interaction behind it.
        int thin = engine.score(pillars(1.0, 1.0, 1.0, 1, 0));
        int solid = engine.score(pillars(1.0, 1.0, 1.0, 60, 0));

        assertThat(thin).isLessThan(solid);
        assertThat(thin).isLessThan(80);   // cannot sprint to the top on one order
        assertThat(solid).isGreaterThan(95);
    }

    @Test
    void oneBadInteractionCannotDestroyAnEstablishedShop() {
        int before = engine.score(pillars(0.95, 0.98, 1.0, 60, 0));
        // Same shop, one failure among many: promise-keeping dips slightly.
        int after = engine.score(pillars(0.95, 0.94, 1.0, 61, -0.01));

        assertThat(before - after).isLessThan(8);
    }

    @Test
    void erraticServiceIsDiscountedButNotDestroyed() {
        int steady = engine.score(pillars(0.9, 0.9, 1.00, 40, 0));
        int erratic = engine.score(pillars(0.9, 0.9, 0.85, 40, 0));

        assertThat(erratic).isLessThan(steady);
        // The floor guarantees the discount stays proportionate.
        assertThat(steady - erratic).isLessThanOrEqualTo(15);
    }

    @Test
    void trajectoryNudgesButNeverDecides() {
        int flat = engine.score(pillars(0.8, 0.8, 1.0, 40, 0));
        int rising = engine.score(pillars(0.8, 0.8, 1.0, 40, weights.getTrajectoryClamp()));
        int falling = engine.score(pillars(0.8, 0.8, 1.0, 40, -weights.getTrajectoryClamp()));

        assertThat(rising).isGreaterThan(flat);
        assertThat(falling).isLessThan(flat);
        assertThat(rising - falling).isLessThanOrEqualTo(11); // ±5 points, no more
    }

    @Test
    void gloriousReviewsCannotRescueAShopThatDoesNotDeliver() {
        // 40% completion rate, but every customer left five stars.
        int withRaves = engine.score(pillars(0.9, 0.40, 1.0, 40, 0), +1.0);
        int withNone  = engine.score(pillars(0.9, 0.40, 1.0, 40, 0),  0.0);

        // Ratings may nudge, never rewrite: 6 points is the hard ceiling.
        assertThat(withRaves - withNone).isLessThanOrEqualTo(6);
        assertThat(withRaves).isLessThan(70);
    }

    @Test
    void reviewBombingCannotDestroyAShopThatDoesDeliver() {
        int bombed = engine.score(pillars(0.95, 0.97, 1.0, 60, 0), -1.0);
        int clean  = engine.score(pillars(0.95, 0.97, 1.0, 60, 0),  0.0);

        assertThat(clean - bombed).isLessThanOrEqualTo(6);
        assertThat(bombed).isGreaterThan(85);
    }

    @Test
    void scoreAlwaysLandsInRange() {
        assertThat(engine.score(pillars(1, 1, 1, 1000, 1))).isBetween(0, 100);
        assertThat(engine.score(pillars(0, 0, 0, 1000, -1))).isBetween(0, 100);
    }

    @Test
    void failingLateIsWeightedMoreHeavilyThanFailingEarly() {
        var c = weights.getCommitment();

        // Ignoring a request the shop opened costs more than never opening it.
        assertThat(c.getViewedThenExpired()).isGreaterThan(c.getExpiredUnseen());
        // Abandoning a committed customer costs most of all.
        assertThat(c.getReservationExpired()).isGreaterThan(c.getMerchantCancelled());
        assertThat(c.getMerchantCancelled()).isGreaterThan(c.getViewedThenExpired());
    }
}
