package com.stockup.backend.domain.reservation.service;

import com.stockup.backend.domain.reservation.dto.CreateReservationRequest;
import com.stockup.backend.domain.reservation.dto.CreateReservationResponse;
import com.stockup.backend.domain.reservation.dto.ReservationDetailResponse;
import com.stockup.backend.domain.reservation.dto.ReservationSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface ReservationService {

    CreateReservationResponse create(CreateReservationRequest request);

    List<ReservationSummaryResponse> getReservations();

    ReservationDetailResponse getReservation(UUID reservationId);
}