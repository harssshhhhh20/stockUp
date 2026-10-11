package com.stockup.backend.infrastructure.geocoding;

import java.util.Optional;

/** Turns a typed postal address into map coordinates. */
public interface AddressGeocoder {

    record Coordinates(double latitude, double longitude) {
    }

    Optional<Coordinates> geocode(
            String street,
            String city,
            String state,
            String postalCode,
            String country
    );
}
