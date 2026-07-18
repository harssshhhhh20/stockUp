package com.stockup.backend.domain.reservation.mapper;

import com.stockup.backend.domain.reservation.dto.ReservationDetailResponse;
import com.stockup.backend.domain.reservation.dto.ReservationSummaryResponse;
import com.stockup.backend.domain.reservation.dto.ReservedItemResponse;
import com.stockup.backend.domain.reservation.model.Reservation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationMapper {

    public ReservationSummaryResponse toSummaryResponse(
            Reservation reservation
    ) {
        return new ReservationSummaryResponse(
                reservation.getId(),
                reservation.getStore().getName(),
                reservation.getStatus(),
                reservation.getReservedUntil(),
                reservation.getCreatedAt()
        );
    }

    public ReservationDetailResponse toDetailResponse(Reservation reservation) {

        List<ReservedItemResponse> items = reservation.getBasket()
                .getItems()
                .stream()
                .map(item -> new ReservedItemResponse(
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnit()
                ))
                .toList();

        return new ReservationDetailResponse(
                reservation.getId(),
                reservation.getStore().getName(),
                reservation.getStatus(),
                reservation.getReservedUntil(),
                reservation.getCreatedAt(),
                items
        );
    }
}
