package com.stockup.backend.domain.reservation.repository;

import com.stockup.backend.domain.reservation.entity.Reservation;
import com.stockup.backend.domain.reservation.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    Optional<Reservation> findByBasketId(UUID basketId);

    Optional<Reservation> findByMerchantOfferId(UUID merchantOfferId);

    List<Reservation> findByStatusAndCreatedAtBefore(ReservationStatus status, Instant createdBefore);

    List<Reservation> findAllByStatusAndActiveAtBefore(ReservationStatus status, Instant activeBefore);

    boolean existsByMerchantOfferId(UUID merchantOfferId);
}
