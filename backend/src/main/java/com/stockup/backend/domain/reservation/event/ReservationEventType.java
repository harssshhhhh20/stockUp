package com.stockup.backend.domain.reservation.event;

/**
 * Every meaningful thing that can happen to a request, in funnel order.
 *
 * These are the raw facts. What they *mean* for trust lives in
 * {@code BharosaWeights} — deliberately not here, so the log stays a record of
 * what happened rather than a record of what we currently think it's worth.
 */
public enum ReservationEventType {

    /** A customer's request reached this shop. */
    REQUEST_BROADCAST,

    /** The shop opened it. */
    MERCHANT_VIEWED,

    /** The shop replied with what it has. */
    OFFER_SUBMITTED,

    /** The shop opened it and then let it lapse — an informed choice to ignore. */
    REQUEST_VIEWED_THEN_EXPIRED,

    /** The request lapsed without the shop ever opening it. */
    REQUEST_EXPIRED_UNSEEN,

    /** The customer picked this shop's offer. */
    CUSTOMER_RESERVED,

    /** The hold went live; an OTP was issued to the customer. */
    RESERVATION_ACTIVATED,

    /** The shop marked the order ready for collection. */
    MERCHANT_READY,

    /** Handed over, OTP verified. */
    HANDOVER_COMPLETED,

    /** The shop backed out after the customer had committed. */
    MERCHANT_CANCELLED,

    /** The customer backed out. Never counts against the shop. */
    CUSTOMER_CANCELLED,

    /** Nobody completed it in time. */
    RESERVATION_EXPIRED,

    /** The customer rated the completed order. */
    FEEDBACK_SUBMITTED
}
