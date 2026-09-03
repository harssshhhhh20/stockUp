package com.stockup.backend.domain.reservation.mapper;

import com.stockup.backend.domain.reservation.dto.ReservationResponse;
import com.stockup.backend.domain.reservation.entity.Reservation;
import com.stockup.backend.domain.store.entity.Store;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    /** Defaults to withholding the pickup code — callers opt in deliberately. */
    public ReservationResponse toResponse(Reservation reservation) {
        return toResponse(reservation, false);
    }

    public ReservationResponse toResponse(Reservation reservation, boolean includeOtp) {
        Store store = reservation.getStore();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getBasket().getId(),
                reservation.getMerchantOffer().getId(),
                reservation.getCustomer().getId(),
                reservation.getMerchant().getId(),
                store.getId(),
                store.getName(),
                formatAddress(store),
                store.getLatitude(),
                store.getLongitude(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getActiveAt(),
                reservation.getNotificationSentAt(),
                reservation.getMerchantViewedAt(),
                includeOtp ? reservation.getOtp() : null,
                reservation.cancellableUntil(),
                reservation.expiresAt()
        );
    }

    /** One line a person could read out to an auto driver. */
    private String formatAddress(Store store) {
        return java.util.stream.Stream.of(
                        store.getAddressLine1(),
                        store.getAddressLine2(),
                        store.getCity(),
                        store.getState(),
                        store.getPostalCode())
                .filter(part -> part != null && !part.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.joining(", "));
    }
}
