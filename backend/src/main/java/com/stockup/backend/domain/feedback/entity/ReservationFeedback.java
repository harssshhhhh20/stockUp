package com.stockup.backend.domain.feedback.entity;

import com.stockup.backend.common.persistence.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * A rating that exists only because an order existed.
 *
 * There is no constructor that doesn't take a reservation, so "verified" is a
 * property of the type rather than a flag somebody has to remember to check.
 */
@Entity
@Table(name = "reservation_feedback")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationFeedback extends AuditableEntity {

    @Column(name = "reservation_id", nullable = false, unique = true)
    private UUID reservationId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(nullable = false)
    private short stars;

    /** Null where the customer chose not to answer that chip. */
    @Column(name = "replied_fast")
    private Boolean repliedFast;

    @Column(name = "ready_on_time")
    private Boolean readyOnTime;

    @Column(name = "stock_accurate")
    private Boolean stockAccurate;

    @Column(length = 1000)
    private String comment;

    private ReservationFeedback(UUID reservationId, UUID customerId, UUID merchantId, UUID storeId,
                               short stars, Boolean repliedFast, Boolean readyOnTime,
                               Boolean stockAccurate, String comment) {
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.merchantId = merchantId;
        this.storeId = storeId;
        this.stars = stars;
        this.repliedFast = repliedFast;
        this.readyOnTime = readyOnTime;
        this.stockAccurate = stockAccurate;
        this.comment = comment;
    }

    public static ReservationFeedback of(UUID reservationId, UUID customerId, UUID merchantId,
                                         UUID storeId, short stars, Boolean repliedFast,
                                         Boolean readyOnTime, Boolean stockAccurate, String comment) {
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Stars must be between 1 and 5.");
        }
        return new ReservationFeedback(reservationId, customerId, merchantId, storeId,
                stars, repliedFast, readyOnTime, stockAccurate, comment);
    }
}
