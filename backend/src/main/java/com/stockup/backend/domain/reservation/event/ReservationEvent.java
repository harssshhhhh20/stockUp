package com.stockup.backend.domain.reservation.event;

import com.stockup.backend.common.persistence.entity.AuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * One immutable fact. No setters, no state transitions — if something changes,
 * that is a new event, not an edit to an old one.
 */
@Entity
@Table(name = "reservation_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationEvent extends AuditableEntity {

    /** Null for funnel events that happen before any reservation exists. */
    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "basket_id", nullable = false)
    private UUID basketId;

    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "merchant_id")
    private UUID merchantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private ReservationEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventActor actor;

    /** When it happened, which for a backfilled row is not when it was written. */
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    /**
     * Postgres jsonb needs the type declared explicitly; without this Hibernate
     * binds a varchar and the insert is rejected.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    private ReservationEvent(
            UUID reservationId,
            UUID basketId,
            UUID storeId,
            UUID merchantId,
            ReservationEventType eventType,
            EventActor actor,
            Instant occurredAt,
            String metadata
    ) {
        this.reservationId = reservationId;
        this.basketId = basketId;
        this.storeId = storeId;
        this.merchantId = merchantId;
        this.eventType = eventType;
        this.actor = actor;
        this.occurredAt = occurredAt;
        this.metadata = metadata;
    }

    public static ReservationEvent of(
            UUID reservationId,
            UUID basketId,
            UUID storeId,
            UUID merchantId,
            ReservationEventType eventType,
            EventActor actor,
            Instant occurredAt,
            String metadata
    ) {
        return new ReservationEvent(
                reservationId, basketId, storeId, merchantId,
                eventType, actor, occurredAt, metadata
        );
    }
}
