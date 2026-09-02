package com.stockup.backend.domain.bharosa;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Every tunable number in the Kasauti model, in one place.
 *
 * Nothing here is hardcoded anywhere else — retuning trust is an edit to
 * application.yml and a restart, never a code change. The defaults below are
 * the values reasoned through in the design spec.
 */
@ConfigurationProperties(prefix = "stockup.bharosa")
@Getter
@Setter
public class BharosaWeights {

    /** How far back scoring looks at all. */
    private int windowDays = 90;

    /** Days for an event to lose half its influence. */
    private double halfLifeDays = 30;

    /**
     * Commitment weights — how much a shop's success or failure mattered, given
     * how far the customer had already invested when it happened.
     */
    private Commitment commitment = new Commitment();

    /** Bayesian shrinkage toward "unproven". */
    private double prior = 0.62;
    private double priorStrength = 6;

    /** At most this many events from any one customer may count. */
    private int perCustomerCap = 3;

    /** Consistency can only ever discount, and only this far. */
    private double consistencyFloor = 0.85;

    /** Trajectory is a nudge, never a verdict. */
    private double trajectoryClamp = 0.05;
    private int trajectoryRecentDays = 14;
    private int trajectoryPriorDays = 45;

    /** Ratings corroborate; this caps how far they can move a score alone. */
    private double corroborationClamp = 0.06;

    /** Response latency at or under this reads as "fast". */
    private int fastResponseSeconds = 300;

    /** Beyond this, responsiveness scores zero — the request window has passed. */
    private int responseWindowSeconds = 900;

    /** Ranking blend exponents. Higher = more influence. */
    private Ranking ranking = new Ranking();

    @Getter
    @Setter
    public static class Commitment {
        /** Never opened it — the customer never knew. */
        private double expiredUnseen = 0.5;
        /** Opened it and ignored it — a choice, so it costs more. */
        private double viewedThenExpired = 1.5;
        /** Answered a request. */
        private double offerSubmitted = 1.0;
        /** Handed the order over. */
        private double handoverCompleted = 3.0;
        /** Backed out after the customer committed. */
        private double merchantCancelled = 4.0;
        /** Left the customer with nothing after they waited. */
        private double reservationExpired = 5.0;
    }

    @Getter
    @Setter
    public static class Ranking {
        private double bharosaExponent = 1.0;
        private double distanceExponent = 0.6;
        private double availabilityExponent = 0.8;
        /** Distance at which relevance falls to 1/e. */
        private double distanceHalfLifeKm = 2.5;
    }
}
