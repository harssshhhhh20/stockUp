package com.stockup.backend.domain.discovery;

import com.stockup.backend.domain.bharosa.BharosaWeights;
import com.stockup.backend.domain.store.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Ranking answers a different question from Bharosa, which is why it lives in a
 * different package.
 *
 *   Bharosa — "how trustworthy is this shop?"
 *   Ranking — "is this the right shop for this request, right now?"
 *
 * Keeping them apart is what stops trust quietly becoming a distance function.
 *
 * The blend is a weighted geometric mean rather than a sum: with a sum, a shop
 * that is very close but unreliable can accumulate enough points to win. With a
 * product, weakness in any dimension drags the whole result down, which matches
 * how someone actually chooses a shop.
 */
@Service
@RequiredArgsConstructor
public class RankingService {

    private final BharosaWeights weights;

    public record Candidate(Store store, int bharosa, double distanceKm, double availabilityMatch) { }

    public record RankedStore(Store store, int bharosa, double distanceKm, double rankScore) { }

    public List<RankedStore> rank(List<Candidate> candidates) {
        return candidates.stream()
                .map(c -> new RankedStore(c.store(), c.bharosa(), c.distanceKm(), score(c)))
                .sorted(Comparator.comparingDouble(RankedStore::rankScore).reversed())
                .toList();
    }

    /**
     * A trusted shop slightly further away should beat a nearby unreliable one —
     * but nobody walks six kilometres for groceries, so distance still bites.
     */
    public double score(Candidate c) {
        var r = weights.getRanking();

        double trust = Math.pow(clamp01(c.bharosa() / 100.0), r.getBharosaExponent());
        double proximity = Math.pow(distanceDecay(c.distanceKm()), r.getDistanceExponent());
        double availability = Math.pow(
                clamp01(c.availabilityMatch()), r.getAvailabilityExponent());

        return trust * proximity * availability;
    }

    /** Relevance falls to 1/e at the configured half-life distance. */
    private double distanceDecay(double km) {
        double scale = weights.getRanking().getDistanceHalfLifeKm();
        return Math.exp(-Math.max(0, km) / scale);
    }

    private static double clamp01(double v) {
        return Math.max(0, Math.min(1, v));
    }
}
