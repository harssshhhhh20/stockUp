package com.stockup.backend.domain.feedback.repository;

import com.stockup.backend.domain.feedback.entity.ReservationFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationFeedbackRepository extends JpaRepository<ReservationFeedback, UUID> {

    Optional<ReservationFeedback> findByReservationId(UUID reservationId);

    boolean existsByReservationId(UUID reservationId);

    Page<ReservationFeedback> findByStoreIdOrderByCreatedAtDesc(UUID storeId, Pageable pageable);

    List<ReservationFeedback> findByMerchantIdAndCreatedAtAfter(UUID merchantId, Instant since);

    @Query("select avg(f.stars) from ReservationFeedback f where f.merchantId = :merchantId")
    Double averageStars(@Param("merchantId") UUID merchantId);

    long countByMerchantId(UUID merchantId);
}
