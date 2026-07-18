package com.stockup.backend.domain.reservation.scheduler;

import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.merchantoffer.entity.MerchantOffer;

import com.stockup.backend.domain.reservation.model.Reservation;
import com.stockup.backend.domain.reservation.model.ReservationStatus;
import com.stockup.backend.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationExpiryScheduler {

    private final ReservationRepository reservationRepository;

    @Transactional
    @Scheduled(fixedDelay = 60_000)
    public void expireReservations() {

        List<Reservation> reservations =
                reservationRepository.findAllByStatusAndReservedUntilBefore(
                        ReservationStatus.CREATED,
                        Instant.now()
                );

        for (Reservation reservation : reservations) {

            reservation.expire();

            reservation.getMerchantOffer().unreserve();

            reservation.getBasket().activate();
        }
    }
}