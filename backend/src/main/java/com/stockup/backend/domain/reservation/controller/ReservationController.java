package com.stockup.backend.domain.reservation.controller;


import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.domain.reservation.dto.CancelReservationRequest;
import com.stockup.backend.domain.reservation.dto.CompleteReservationRequest;
import com.stockup.backend.domain.reservation.dto.ReservationResponse;
import com.stockup.backend.domain.reservation.entity.enums.ReservationStatus;
import com.stockup.backend.domain.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReservationResponse>>> getReservations(
            @RequestParam(defaultValue = "ACTIVE")
            ReservationStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.RESERVATIONS_FETCHED,
                reservationService.getReservations(status, pageable)
        );
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservation(
            @PathVariable UUID reservationId
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.RESERVATION_FETCHED,
                reservationService.getReservation(reservationId)
        );
    }

    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(
            @PathVariable UUID reservationId,
            @Valid @RequestBody CancelReservationRequest request
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.UPDATED,
                reservationService.cancelReservation(reservationId, request)
        );
    }

    @PostMapping("/{reservationId}/complete")
    public ResponseEntity<ApiResponse<ReservationResponse>> completeReservation(
            @PathVariable UUID reservationId,
            @Valid @RequestBody CompleteReservationRequest request
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.UPDATED,
                reservationService.completeReservation(reservationId, request)
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> reserveMerchantOffer(
            @RequestParam UUID merchantOfferId
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.RESERVATION_SUCCESS,
                reservationService.reserveMerchantOffer(merchantOfferId)
        );
    }

}
