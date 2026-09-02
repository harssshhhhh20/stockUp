package com.stockup.backend.domain.reservation.event;

public enum EventActor {
    CUSTOMER,
    MERCHANT,
    /** Schedulers and automatic transitions. */
    SYSTEM
}
