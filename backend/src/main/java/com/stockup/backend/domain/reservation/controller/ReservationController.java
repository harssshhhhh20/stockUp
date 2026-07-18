package com.stockup.backend.domain.reservation.controller;

import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.domain.reservation.dto.CreateReservationRequest;
import com.stockup.backend.domain.reservation.dto.CreateReservationResponse;
import com.stockup.backend.domain.reservation.dto.ReservationDetailResponse;
import com.stockup.backend.domain.reservation.dto.ReservationSummaryResponse;
import com.stockup.backend.domain.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateReservationResponse>> createReservation(
            @Valid @RequestBody CreateReservationRequest request
    ) {

        CreateReservationResponse response =
                reservationService.create(request);

        return ApiResponseFactory.success(ResponseMessage.RESERVATION_SUCCESS, response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationSummaryResponse>>> getReservations() {

        List<ReservationSummaryResponse> response =
                reservationService.getReservations();

        return ApiResponseFactory.success(
                ResponseMessage.RESERVATIONS_FETCHED,
                response
        );
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<ReservationDetailResponse>> getReservation(
            @PathVariable UUID reservationId
    ) {

        ReservationDetailResponse response =
                reservationService.getReservation(reservationId);

        return ApiResponseFactory.success(
                ResponseMessage.RESERVATION_FETCHED,
                response
        );
    }
}