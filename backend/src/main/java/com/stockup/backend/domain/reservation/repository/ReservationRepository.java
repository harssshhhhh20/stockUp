package com.stockup.backend.domain.reservation.repository;

import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.merchantoffer.entity.MerchantOffer;
import com.stockup.backend.domain.reservation.model.Reservation;
import com.stockup.backend.domain.reservation.model.ReservationStatus;
import com.stockup.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    boolean existsByBasketAndStatus(
            Basket basket,
            ReservationStatus status
    );

    Optional<Reservation> findByMerchantOffer(MerchantOffer merchantOffer);

    List<Reservation> findAllByStatusAndReservedUntilBefore(
            ReservationStatus status,
            Instant instant
    );
    List<Reservation> findAllByBasketCustomerOrderByCreatedAtDesc(User customer);

    Optional<Reservation> findByIdAndCustomer(UUID id, User customer);
}