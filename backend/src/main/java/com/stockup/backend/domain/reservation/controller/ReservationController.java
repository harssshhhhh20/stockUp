package com.stockup.backend.domain.reservation.controller;


import com.stockup.backend.domain.reservation.dto.CancelReservationRequest;
import com.stockup.backend.domain.reservation.dto.CompleteReservationRequest;
import com.stockup.backend.domain.reservation.dto.ReservationResponse;
import com.stockup.backend.domain.reservation.entity.enums.ReservationStatus;
import com.stockup.backend.domain.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public Page<ReservationResponse> getReservations(
            @RequestParam(defaultValue = "ACTIVE")
            ReservationStatus status,
            Pageable pageable
    ) {
        return reservationService.getReservations(
                status,
                pageable
        );
    }

    @GetMapping("/{reservationId}")
    public ReservationResponse getReservation(
            @PathVariable UUID reservationId
    ) {
        return reservationService.getReservation(reservationId);
    }

    @PostMapping("/{reservationId}/cancel")
    public ReservationResponse cancelReservation(
            @PathVariable UUID reservationId,
            @Valid @RequestBody CancelReservationRequest request
    ) {
        return reservationService.cancelReservation(
                reservationId,
                request
        );
    }
    @PostMapping("/{reservationId}/complete")
    public ReservationResponse completeReservation(
            @PathVariable UUID reservationId,
            @Valid @RequestBody CompleteReservationRequest request
    ) {
        return reservationService.completeReservation(
                reservationId,
                request
        );
    }

    @PostMapping
    public ReservationResponse reserveMerchantOffer(
            @RequestParam UUID merchantOfferId
    ) {
        return reservationService.reserveMerchantOffer(
                merchantOfferId
        );
    }

}
