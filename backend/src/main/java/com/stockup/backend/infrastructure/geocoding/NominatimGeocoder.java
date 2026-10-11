package com.stockup.backend.infrastructure.geocoding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * OpenStreetMap's free geocoder, used only when a shopkeeper types their address
 * instead of sharing their location.
 *
 * The phone can't do this itself: Android's geocoder refuses to run without
 * location permission, which is exactly the case this exists for. Shop sign-up
 * is rare, so Nominatim's one-request-a-second policy is never close; it does
 * require an identifying User-Agent.
 *
 * Small Indian streets are often missing from OSM, so the lookup falls back to
 * the PIN code — a few km out at worst, and the shopkeeper can always re-pin
 * with "Use my current location". It never falls back to the city alone: a
 * city centre can be 15 km from the shop, which would put it in the wrong
 * neighbourhood's "nearby" results.
 */
@Component
public class NominatimGeocoder implements AddressGeocoder {

    private static final Logger log = LoggerFactory.getLogger(NominatimGeocoder.class);

    private record Place(String lat, String lon) {
    }

    private final RestClient restClient;

    public NominatimGeocoder(
            RestClient.Builder builder,
            @Value("${stockup.geocoding.base-url:https://nominatim.openstreetmap.org}") String baseUrl,
            @Value("${stockup.geocoding.user-agent:StockUp/1.0 (shop address lookup)}") String userAgent
    ) {
        // A geocoder that hangs would hang shop sign-up with it.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(8));

        this.restClient = builder
                .requestFactory(factory)
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", userAgent)
                .build();
    }

    @Override
    public Optional<Coordinates> geocode(
            String street,
            String city,
            String state,
            String postalCode,
            String country
    ) {
        Optional<Coordinates> exact = search(uri -> uri
                .queryParam("street", street)
                .queryParam("city", city)
                .queryParam("state", state)
                .queryParam("postalcode", postalCode)
                .queryParam("country", country)
                .build());

        if (exact.isPresent()) {
            return exact;
        }

        return search(uri -> uri
                .queryParam("postalcode", postalCode)
                .queryParam("country", country)
                .build());
    }

    private Optional<Coordinates> search(Function<UriBuilder, URI> query) {
        try {
            List<Place> places = restClient.get()
                    .uri(uri -> query.apply(uri
                            .path("/search")
                            .queryParam("format", "jsonv2")
                            .queryParam("limit", 1)))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Place>>() {
                    });

            if (places == null || places.isEmpty()) {
                return Optional.empty();
            }

            Place place = places.getFirst();
            return Optional.of(new Coordinates(
                    Double.parseDouble(place.lat()),
                    Double.parseDouble(place.lon())
            ));

        } catch (Exception ex) {
            // Treated as "not found": the caller asks the shopkeeper to use
            // their current location instead, which always works.
            log.warn("Geocoding failed", ex);
            return Optional.empty();
        }
    }
}
