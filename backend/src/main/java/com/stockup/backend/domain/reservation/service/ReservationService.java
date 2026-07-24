package com.stockup.backend.domain.reservation.service;

import com.stockup.backend.domain.reservation.dto.CancelReservationRequest;
import com.stockup.backend.domain.reservation.dto.CompleteReservationRequest;
import com.stockup.backend.domain.reservation.dto.ReservationResponse;

import java.util.UUID;

public interface ReservationService {

    ReservationResponse reserveMerchantOffer(UUID merchantOfferId);

    ReservationResponse getReservation(UUID reservationId);

    ReservationResponse cancelByCustomer(
            UUID reservationId,
            CancelReservationRequest request
    );

    ReservationResponse cancelByMerchant(
            UUID reservationId,
            CancelReservationRequest request
    );

    ReservationResponse completeReservation(
            UUID reservationId,
            CompleteReservationRequest request
    );

    void activateReservation(UUID reservationId);

    void expireReservation(UUID reservationId);
}