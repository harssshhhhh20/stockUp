package com.stockup.backend.domain.reservation.entity;

import com.stockup.backend.domain.reservation.entity.enums.ReservationStatus;
import com.stockup.backend.domain.reservation.exception.InvalidCancellationReasonException;
import com.stockup.backend.domain.reservation.exception.InvalidOtpException;
import com.stockup.backend.domain.reservation.exception.InvalidReservationStateException;
import com.stockup.backend.domain.reservation.exception.MerchantCancellationWindowClosedException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    private Reservation pending() {
        return Reservation.create(null, null, null, null, null);
    }

    private Reservation active() {
        Reservation reservation = pending();
        reservation.activate("123456");
        return reservation;
    }

    @Test
    void startsPendingNotification() {
        assertThat(pending().getStatus()).isEqualTo(ReservationStatus.PENDING_NOTIFICATION);
    }

    @Test
    void activationStoresTheOtpAndTimestamps() {
        Reservation reservation = active();

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(reservation.getActiveAt()).isNotNull();
        assertThat(reservation.getNotificationSentAt()).isNotNull();
    }

    @Test
    void activationRejectsAMalformedOtp() {
        assertThatThrownBy(() -> pending().activate("12"))
                .isInstanceOf(InvalidOtpException.class);
    }

    @Test
    void completingRequiresTheMatchingOtpAndThenClearsIt() {
        Reservation reservation = active();

        assertThatThrownBy(() -> reservation.complete("000000"))
                .isInstanceOf(InvalidOtpException.class);

        reservation.complete("123456");

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
        assertThat(reservation.getOtp()).isNull();
    }

    @Test
    void customerMayOnlyCancelBeforeItGoesActive() {
        Reservation reservation = pending();
        reservation.cancelByCustomer("Changed my mind");
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CUSTOMER_CANCELLED);

        assertThatThrownBy(() -> active().cancelByCustomer("Too late"))
                .isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void cancellationAlwaysNeedsAReason() {
        assertThatThrownBy(() -> pending().cancelByCustomer("  "))
                .isInstanceOf(InvalidCancellationReasonException.class);

        assertThatThrownBy(() -> active().cancelByMerchant(""))
                .isInstanceOf(InvalidCancellationReasonException.class);
    }

    @Test
    void merchantCanCancelWhileThereIsStillTimeToTellTheCustomer() {
        Reservation reservation = active();

        reservation.cancelByMerchant("Sold out");

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.MERCHANT_CANCELLED);
    }

    @Test
    void merchantCannotCancelInsideTheLockoutBeforeExpiry() {
        Reservation reservation = active();

        // Wind the clock forward to inside the final lockout window.
        Instant almostExpired = Instant.now()
                .minus(Reservation.ACTIVE_TTL)
                .plus(Reservation.MERCHANT_CANCELLATION_LOCKOUT)
                .minus(Duration.ofSeconds(30));
        ReflectionTestUtils.setField(reservation, "activeAt", almostExpired);

        assertThatThrownBy(() -> reservation.cancelByMerchant("Sold out"))
                .isInstanceOf(MerchantCancellationWindowClosedException.class);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
    }

    @Test
    void expiryOnlyAppliesToActiveReservations() {
        Reservation reservation = active();
        reservation.expire();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.EXPIRED);

        assertThatThrownBy(() -> pending().expire())
                .isInstanceOf(InvalidReservationStateException.class);
    }
}
