package com.stockup.backend.domain.reservation.service;

import com.stockup.backend.domain.reservation.dto.CancelReservationRequest;
import com.stockup.backend.domain.reservation.dto.CompleteReservationRequest;
import com.stockup.backend.domain.reservation.dto.ReservationResponse;
import com.stockup.backend.domain.reservation.entity.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReservationService {

    ReservationResponse reserveMerchantOffer(UUID merchantOfferId);

    ReservationResponse getReservation(UUID reservationId);

    ReservationResponse completeReservation(
            UUID reservationId,
            CompleteReservationRequest request
    );

    void activateReservation(UUID reservationId);

    void expireReservation(UUID reservationId);

    Page<ReservationResponse> getReservations(
            ReservationStatus status,
            Pageable pageable
    );

    ReservationResponse cancelReservation(
      UUID reservationId,
      CancelReservationRequest request
    );
}