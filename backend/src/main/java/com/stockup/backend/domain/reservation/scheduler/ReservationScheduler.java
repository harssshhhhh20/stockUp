package com.stockup.backend.domain.reservation.scheduler;

import com.stockup.backend.domain.reservation.entity.Reservation;
import com.stockup.backend.domain.reservation.entity.enums.ReservationStatus;
import com.stockup.backend.domain.reservation.repository.ReservationRepository;
import com.stockup.backend.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    @Scheduled(fixedDelay = 60_000)
    public void activateReservations() {

        List<Reservation> reservations =
                reservationRepository.findByStatusAndCreatedAtBefore(
                        ReservationStatus.PENDING_NOTIFICATION,
                        Instant.now().minus(Reservation.ACTIVATION_DELAY)
                );

        for (Reservation reservation : reservations) {
            try {
                reservationService.activateReservation(reservation.getId());
            } catch (Exception ex) {
                log.error(
                        "Failed to activate reservation {}",
                        reservation.getId(),
                        ex
                );
            }
        }
    }
}
