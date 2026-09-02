package com.stockup.backend.domain.bharosa;

import com.stockup.backend.domain.feedback.entity.ReservationFeedback;
import com.stockup.backend.domain.feedback.repository.ReservationFeedbackRepository;
import com.stockup.backend.domain.reservation.event.ReservationEventRepository;
import com.stockup.backend.domain.reservation.event.ReservationEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Kasauti — the engine that turns a shop's behaviour into a Bharosa score.
 *
 * The shape of this deliberately resists the obvious shortcuts:
 *
 *  - outcomes are aggregated per customer, not per event, so one enthusiastic
 *    (or malicious) person cannot manufacture a reputation;
 *  - responsiveness and promise-keeping combine by harmonic mean, so a shop
 *    that answers instantly and never delivers cannot average its way to
 *    respectability;
 *  - everything decays, so past glory does not hold a score up;
 *  - thin evidence is shrunk toward an unproven prior rather than trusted.
 *
 * Every constant lives in {@link BharosaWeights}.
 */
@Service
@RequiredArgsConstructor
public class BharosaEngine {

    private final ReservationEventRepository events;
    private final ReservationFeedbackRepository feedback;
    private final BharosaWeights weights;

    /** One customer's collapsed history with one shop. */
    private static final class CustomerTally {
        double answeredWeight;      // decayed weight of requests answered
        double receivedWeight;      // decayed weight of requests received
        double keptWeight;          // decayed weight of promises kept
        double committedWeight;     // decayed weight of promises made
        int events;
        /**
         * Which of this customer's requests are allowed to count. Capping by
         * interaction rather than by raw event matters: events arrive in funnel
         * order, so an event-count cap would keep the broadcast and throw away
         * the handover — scoring every shop as though it never delivered.
         */
        final Set<UUID> countedBaskets = new HashSet<>();
    }

    public BharosaPillars computePillars(UUID merchantId) {
        Instant since = Instant.now().minus(Duration.ofDays(weights.getWindowDays()));
        var rows = events.findScoreableEvents(merchantId, since);

        Map<UUID, CustomerTally> byCustomer = new HashMap<>();

        for (var row : rows) {
            CustomerTally t = byCustomer.computeIfAbsent(row.getCustomerId(), k -> new CustomerTally());

            // The per-customer cap is the anti-gaming backbone: beyond this many
            // separate requests, one person simply stops adding weight. Whole
            // interactions are admitted or skipped, never half of one.
            UUID basket = row.getBasketId();
            if (!t.countedBaskets.contains(basket)) {
                if (t.countedBaskets.size() >= weights.getPerCustomerCap()) continue;
                t.countedBaskets.add(basket);
            }
            t.events++;

            double decay = decay(row.getOccurredAt());
            ReservationEventType type;
            try {
                type = ReservationEventType.valueOf(row.getEventType());
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            var c = weights.getCommitment();

            switch (type) {
                case REQUEST_BROADCAST -> t.receivedWeight += c.getOfferSubmitted() * decay;
                case OFFER_SUBMITTED -> t.answeredWeight += c.getOfferSubmitted() * decay;
                case REQUEST_EXPIRED_UNSEEN -> t.receivedWeight += c.getExpiredUnseen() * decay;
                case REQUEST_VIEWED_THEN_EXPIRED -> t.receivedWeight += c.getViewedThenExpired() * decay;

                case HANDOVER_COMPLETED -> {
                    t.keptWeight += c.getHandoverCompleted() * decay;
                    t.committedWeight += c.getHandoverCompleted() * decay;
                }
                case MERCHANT_CANCELLED -> t.committedWeight += c.getMerchantCancelled() * decay;
                case RESERVATION_EXPIRED -> t.committedWeight += c.getReservationExpired() * decay;

                // Customer-side withdrawal is never the shop's fault.
                case CUSTOMER_CANCELLED -> { }
                default -> { }
            }
        }

        if (byCustomer.isEmpty()) {
            return new BharosaPillars(0, 0, 1, 0, 0, null, 0, 0);
        }

        // Average across customers — one shopper, one voice.
        List<Double> perCustomerScores = new ArrayList<>();
        double responsivenessSum = 0, responsivenessN = 0;
        double promiseSum = 0, promiseN = 0;
        double nEff = 0;
        long totalEvents = 0;

        for (CustomerTally t : byCustomer.values()) {
            totalEvents += t.events;
            // Confidence counts interactions, not raw event rows — otherwise a
            // chatty funnel would look like more evidence than it is.
            nEff += t.countedBaskets.size();

            Double r = t.receivedWeight > 0 ? t.answeredWeight / t.receivedWeight : null;
            Double v = t.committedWeight > 0 ? t.keptWeight / t.committedWeight : null;

            if (r != null) { responsivenessSum += clamp01(r); responsivenessN++; }
            if (v != null) { promiseSum += clamp01(v); promiseN++; }

            // For dispersion, use whichever dimensions this customer actually exercised.
            if (r != null || v != null) {
                double blended = (r != null && v != null) ? (r + v) / 2 : (r != null ? r : v);
                perCustomerScores.add(clamp01(blended));
            }
        }

        double responsiveness = responsivenessN > 0 ? responsivenessSum / responsivenessN : 0;
        double promiseKeeping = promiseN > 0 ? promiseSum / promiseN : 0;

        // Speed modulates responsiveness but cannot replace actually answering.
        Double medianSeconds = events.medianResponseSeconds(merchantId, since);
        responsiveness = responsiveness * (0.6 + 0.4 * speedFactor(medianSeconds));

        double consistency = consistency(perCustomerScores);
        long distinct = events.distinctCompletedCustomers(merchantId, since);

        return new BharosaPillars(
                clamp01(responsiveness),
                clamp01(promiseKeeping),
                consistency,
                nEff,
                trajectory(merchantId),
                medianSeconds,
                distinct,
                totalEvents
        );
    }

    /**
     * Combines the pillars into 0..100.
     *
     * The harmonic mean is the important part: it collapses toward whichever of
     * responsiveness and promise-keeping is weaker, so strength in one cannot
     * buy forgiveness for failure in the other.
     */
    public int score(BharosaPillars p) {
        return score(p, 0.0);
    }

    /**
     * @param corroboration −1..+1, how far customers agree with what we observed.
     *                      Bounded hard, because ratings corroborate a record —
     *                      they do not create one. A shop cannot buy its way up
     *                      with reviews, and cannot be review-bombed down.
     */
    public int score(BharosaPillars p, double corroboration) {
        double core = harmonic(p.responsiveness(), p.promiseKeeping());

        double shrunk =
                (core * p.confidence() + weights.getPrior() * weights.getPriorStrength())
                        / (p.confidence() + weights.getPriorStrength());

        double clamp = weights.getCorroborationClamp();
        double feedbackTerm = Math.max(-clamp, Math.min(clamp, corroboration * clamp));

        double adjusted = shrunk * p.consistency() + p.trajectory() + feedbackTerm;

        return (int) Math.round(clamp01(adjusted) * 100);
    }

    public int scoreFor(UUID merchantId) {
        return score(computePillars(merchantId), corroboration(merchantId));
    }

    /**
     * How far verified customer feedback agrees with the behavioural record.
     *
     * Returns −1..+1. Positive where customers confirm what we saw; negative
     * where they contradict it — which is the signature worth catching, a shop
     * marking orders complete that customers say never happened.
     */
    public double corroboration(UUID merchantId) {
        Instant since = Instant.now().minus(Duration.ofDays(weights.getWindowDays()));
        List<ReservationFeedback> rows = feedback.findByMerchantIdAndCreatedAtAfter(merchantId, since);
        if (rows.size() < 3) return 0; // too little to corroborate anything

        double sum = 0;
        int n = 0;
        for (ReservationFeedback f : rows) {
            // Stars, recentred so 3/5 is neutral rather than positive.
            sum += (f.getStars() - 3) / 2.0;
            n++;

            // Chips are worth more than stars: they are specific claims about
            // the same dimensions the pillars measure.
            for (Boolean chip : List.of(
                    Boolean.TRUE.equals(f.getRepliedFast()),
                    Boolean.TRUE.equals(f.getReadyOnTime()),
                    Boolean.TRUE.equals(f.getStockAccurate()))) {
                sum += chip ? 0.5 : -0.5;
                n++;
            }
        }
        return n == 0 ? 0 : Math.max(-1, Math.min(1, sum / n));
    }

    // ---- pieces -------------------------------------------------------------

    /** Weight halves every {@code halfLifeDays}. */
    private double decay(Instant occurredAt) {
        double ageDays = Duration.between(occurredAt, Instant.now()).toSeconds() / 86_400.0;
        return Math.pow(0.5, Math.max(0, ageDays) / weights.getHalfLifeDays());
    }

    /** 1.0 when answered instantly, 0 once the request window has passed. */
    private double speedFactor(Double medianSeconds) {
        if (medianSeconds == null) return 0.5; // no evidence either way
        if (medianSeconds <= weights.getFastResponseSeconds()) return 1.0;
        if (medianSeconds >= weights.getResponseWindowSeconds()) return 0.0;
        double span = weights.getResponseWindowSeconds() - weights.getFastResponseSeconds();
        return 1.0 - ((medianSeconds - weights.getFastResponseSeconds()) / span);
    }

    /**
     * Steady shops beat erratic ones. Standard deviation across per-customer
     * outcomes, mapped into a multiplier that can discount but never destroy.
     */
    private double consistency(List<Double> perCustomer) {
        if (perCustomer.size() < 2) return 1.0;
        double mean = perCustomer.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = perCustomer.stream()
                .mapToDouble(s -> (s - mean) * (s - mean))
                .average().orElse(0);
        double sd = Math.sqrt(variance);
        double floor = weights.getConsistencyFloor();
        // sd of 0.5 is about as scattered as a 0..1 measure gets.
        return Math.max(floor, 1.0 - (1.0 - floor) * Math.min(1.0, sd / 0.5));
    }

    /** Recent form versus older form, clamped so it nudges rather than decides. */
    private double trajectory(UUID merchantId) {
        Instant now = Instant.now();
        double recent = completionRate(merchantId,
                now.minus(Duration.ofDays(weights.getTrajectoryRecentDays())), now);
        double older = completionRate(merchantId,
                now.minus(Duration.ofDays(weights.getTrajectoryPriorDays())),
                now.minus(Duration.ofDays(weights.getTrajectoryRecentDays())));

        if (Double.isNaN(recent) || Double.isNaN(older)) return 0;

        double clamp = weights.getTrajectoryClamp();
        return Math.max(-clamp, Math.min(clamp, recent - older));
    }

    private double completionRate(UUID merchantId, Instant from, Instant to) {
        long kept = events.countByMerchantIdAndEventTypeAndOccurredAtAfter(
                merchantId, ReservationEventType.HANDOVER_COMPLETED, from);
        long cancelled = events.countByMerchantIdAndEventTypeAndOccurredAtAfter(
                merchantId, ReservationEventType.MERCHANT_CANCELLED, from);
        long expired = events.countByMerchantIdAndEventTypeAndOccurredAtAfter(
                merchantId, ReservationEventType.RESERVATION_EXPIRED, from);
        long total = kept + cancelled + expired;
        return total == 0 ? Double.NaN : (double) kept / total;
    }

    private static double harmonic(double a, double b) {
        if (a <= 0 || b <= 0) return 0;
        return 2 * a * b / (a + b);
    }

    private static double clamp01(double v) {
        return Math.max(0, Math.min(1, v));
    }
}
