package com.stockup.backend.domain.reservation.model;

import com.stockup.backend.common.persistence.entity.AuditableEntity;
import com.stockup.backend.domain.basket.entity.Basket;

import com.stockup.backend.domain.merchantoffer.entity.MerchantOffer;

import com.stockup.backend.domain.reservation.exception.InvalidReservationStateException;
import com.stockup.backend.domain.store.entity.Store;

import com.stockup.backend.domain.user.entity.User;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "reservations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reservation_merchant_offer",
                        columnNames = "merchant_offer_id"
                ),
                @UniqueConstraint(
                        name = "uk_reservation_basket",
                        columnNames = "basket_id"
                )
        }
)
public class Reservation extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "basket_id", nullable = false)
    private Basket basket;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_offer_id", nullable = false)
    private MerchantOffer merchantOffer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "reserved_until", nullable = false)
    private Instant reservedUntil;

    protected Reservation() {
    }

    public static Reservation create(
            Basket basket,
            MerchantOffer merchantOffer,
            User customer,
            Store store,
            Instant reservedUntil
    ){
        Reservation reservation = new Reservation();
        reservation.basket = basket;
        reservation.merchantOffer = merchantOffer;
        reservation.customer = customer;
        reservation.store = store;
        reservation.status = ReservationStatus.CREATED;
        reservation.reservedUntil = reservedUntil;

        return reservation;
    }

    public void complete() {
        validateStatus(ReservationStatus.CREATED);
        this.status = ReservationStatus.COMPLETED;
    }

    public void cancel() {
        validateStatus(ReservationStatus.CREATED);
        this.status = ReservationStatus.CANCELLED;
    }

    public void expire() {
        validateStatus(ReservationStatus.CREATED);
        this.status = ReservationStatus.EXPIRED;
    }

    private void validateStatus(ReservationStatus expectedStatus) {
        if (this.status != expectedStatus) {
            throw new InvalidReservationStateException(
                    "Reservation must be in " + expectedStatus + " status."
            );
        }
    }

    public Basket getBasket() {
        return basket;
    }

    public MerchantOffer getMerchantOffer() {
        return merchantOffer;
    }

    public User getCustomer() {
        return customer;
    }

    public Store getStore() {
        return store;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Instant getReservedUntil() {
        return reservedUntil;
    }
}