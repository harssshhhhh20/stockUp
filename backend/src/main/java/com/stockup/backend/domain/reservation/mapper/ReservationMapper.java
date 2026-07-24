package com.stockup.backend.domain.reservation.mapper;

import com.stockup.backend.domain.reservation.dto.ReservationResponse;
import com.stockup.backend.domain.reservation.entity.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getBasket().getId(),
                reservation.getMerchantOffer().getId(),
                reservation.getCustomer().getId(),
                reservation.getMerchant().getId(),
                reservation.getStore().getId(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getActiveAt(),
                reservation.getNotificationSentAt(),
                reservation.getViewedAt()
        );
    }
}
