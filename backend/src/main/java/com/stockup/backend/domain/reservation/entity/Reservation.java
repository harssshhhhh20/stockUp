package com.stockup.backend.domain.reservation.entity;

import com.stockup.backend.common.persistence.entity.AuditableEntity;
import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.merchantoffer.entity.MerchantOffer;
import com.stockup.backend.domain.reservation.entity.enums.ReservationStatus;
import com.stockup.backend.domain.reservation.exception.*;
import com.stockup.backend.domain.store.entity.Store;
import com.stockup.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "reservations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reservation_basket",
                        columnNames = "basket_id"
                ),
                @UniqueConstraint(
                        name = "uk_reservation_merchant_offer",
                        columnNames = "merchant_offer_id"
                )
        }
)
public class Reservation extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "basket_id", nullable = false, updatable = false)
    private Basket basket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_offer_id", nullable = false, updatable = false)
    private MerchantOffer merchantOffer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, updatable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false, updatable = false)
    private Merchant merchant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false, updatable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    /**
     * Time when the reservation notification was sent to the merchant.
     * Populated when the activation scheduler runs.
     */
    @Column(name = "notification_sent_at")
    private Instant notificationSentAt;

    /**
     * First time the merchant viewed the reservation notification.
     * Used only for analytics and merchant reputation.
     */
    @Column(name = "viewed_at")
    private Instant merchantViewedAt;

    /**
     * Time when the reservation became active.
     */
    @Column(name = "active_at")
    private Instant activeAt;

    /**
     * Reason supplied when the reservation is cancelled.
     */
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    /**
     * OTP used for order handover.
     */
    @Column(name = "otp", length = 6)
    private String otp;

    public static Reservation create(
            Basket basket,
            MerchantOffer merchantOffer,
            User customer,
            Merchant merchant,
            Store store
    ) {
        Reservation reservation = new Reservation();

        reservation.basket = basket;
        reservation.merchantOffer = merchantOffer;
        reservation.customer = customer;
        reservation.merchant = merchant;
        reservation.store = store;
        reservation.status = ReservationStatus.PENDING_NOTIFICATION;

        return reservation;
    }

    private void requireStatus(ReservationStatus expected) {
        if (status != expected) {
            throw new InvalidReservationStateException(
                    "Expected reservation status " + expected + " but was " + status
            );
        }
    }

    /**
     * Called by the scheduler after the pending-notification window.
     */
    public void activate(String otp) {
        requireStatus(ReservationStatus.PENDING_NOTIFICATION);
        if (otp == null || !otp.matches("\\d{6}")) {
            throw new InvalidOtpException(
                    "OTP must be exactly 6 digits."
            );
        }
        Instant now = Instant.now();
        this.otp = otp;
        activeAt = now;
        notificationSentAt = now;
        status = ReservationStatus.ACTIVE;
    }

    /**
     * Records the first time the merchant views the reservation details.
     */
    public void markViewedByMerchant() {
        requireStatus(ReservationStatus.ACTIVE);
        if (merchantViewedAt == null) {
            merchantViewedAt = Instant.now();
        }
    }

    public void cancelByCustomer(String reason) {
        requireStatus(ReservationStatus.PENDING_NOTIFICATION);

        validateCancellationReason(reason);

        cancellationReason = reason;
        status = ReservationStatus.CUSTOMER_CANCELLED;
    }

    public void cancelByMerchant(String reason) {
        requireStatus(ReservationStatus.ACTIVE);

        validateCancellationReason(reason);

        cancellationReason = reason;
        status = ReservationStatus.MERCHANT_CANCELLED;
    }

    public void complete(String otp) {
        requireStatus(ReservationStatus.ACTIVE);

        if (this.otp == null) {
            throw new OtpNotGeneratedException(
                    "OTP has not been assigned."
            );
        }

        if (!this.otp.equals(otp)) {
            throw new InvalidOtpException(
                    "Invalid OTP."
            );
        }

        status = ReservationStatus.COMPLETED;
        this.otp = null;
    }

    public void expire() {
        requireStatus(ReservationStatus.ACTIVE);

        status = ReservationStatus.EXPIRED;
    }

    private void validateCancellationReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new InvalidCancellationReasonException(
                    "Cancellation reason is required."
            );
        }
    }

    public void validateCustomer(User customer) {
        if (!this.customer.equals(customer)) {
            throw new ReservationAccessDeniedException(
                    "You are not authorized to access this reservation."
            );
        }
    }

    public void validateMerchant(Merchant merchant) {
        if (!this.merchant.equals(merchant)) {
            throw new ReservationAccessDeniedException(
                    "You are not authorized to access this reservation."
            );
        }
    }
}