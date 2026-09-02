package com.stockup.backend.domain.discovery;

import com.stockup.backend.domain.bharosa.BharosaEngine;
import com.stockup.backend.domain.bharosa.BharosaExplainer;
import com.stockup.backend.domain.bharosa.BharosaPillars;
import com.stockup.backend.domain.discovery.dto.NearbyStoreResponse;
import com.stockup.backend.domain.store.entity.Store;
import com.stockup.backend.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Finds shops worth asking.
 *
 * This is where Bharosa stops being a number on a card and starts changing what
 * people see first — which is the only place a trust score actually earns its
 * keep. Scoring says how trustworthy a shop is; ranking decides whether it is
 * the right shop for *this* request, and the two stay in separate classes.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscoveryService {

    private final StoreRepository storeRepository;
    private final BharosaEngine engine;
    private final BharosaExplainer explainer;
    private final RankingService rankingService;

    public List<NearbyStoreResponse> nearby(double latitude, double longitude, int radiusMeters) {

        List<Store> stores = storeRepository.findNearbyStores(
                BigDecimal.valueOf(latitude),
                BigDecimal.valueOf(longitude),
                radiusMeters
        );

        List<RankingService.Candidate> candidates = stores.stream()
                .map(store -> {
                    double km = haversineKm(
                            latitude, longitude,
                            store.getLatitude(), store.getLongitude());
                    int score = engine.scoreFor(store.getMerchant().getId());
                    // Without a per-request item list there is nothing to match
                    // against yet, so availability stays neutral rather than
                    // inventing a number that would quietly skew the ranking.
                    return new RankingService.Candidate(store, score, km, 1.0);
                })
                .toList();

        return rankingService.rank(candidates).stream()
                .map(ranked -> {
                    Store store = ranked.store();
                    BharosaPillars pillars = engine.computePillars(store.getMerchant().getId());
                    return new NearbyStoreResponse(
                            store.getId(),
                            store.getName(),
                            store.getBusinessType(),
                            store.getCity(),
                            round1(ranked.distanceKm()),
                            ranked.bharosa(),
                            band(ranked.bharosa(), pillars),
                            explainer.bannerTags(pillars),
                            ranked.rankScore()
                    );
                })
                .toList();
    }

    private String band(int score, BharosaPillars pillars) {
        if (pillars.isUnproven()) return "new";
        if (score >= 75) return "trusted";
        if (score >= 45) return "mixed";
        return "risky";
    }

    /** Great-circle distance; the store search itself already filters by radius. */
    private double haversineKm(double lat1, double lon1, Double lat2, Double lon2) {
        if (lat2 == null || lon2 == null) return Double.MAX_VALUE;
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
