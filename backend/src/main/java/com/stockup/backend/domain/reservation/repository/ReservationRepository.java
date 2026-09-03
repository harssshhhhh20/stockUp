package com.stockup.backend.domain.reservation.repository;

import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.reservation.entity.Reservation;
import com.stockup.backend.domain.reservation.entity.enums.ReservationStatus;
import com.stockup.backend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Reservation> findByMerchantAndStatus(
            Merchant merchant,
            ReservationStatus status,
            Pageable pageable
    );

    Page<Reservation> findByCustomerAndStatus(
            User customer,
            ReservationStatus status,
            Pageable pageable
    );

    /** Every reservation on this side of the marketplace — the "All" filter. */
    Page<Reservation> findByMerchant(Merchant merchant, Pageable pageable);

    Page<Reservation> findByCustomer(User customer, Pageable pageable);

    Page<Reservation> findAllByStatus(
            ReservationStatus status,
            Pageable pageable
    );
}
