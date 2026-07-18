package com.stockup.backend.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateReservationRequest(

        @NotNull
        UUID merchantOfferId

) {
}