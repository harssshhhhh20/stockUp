package com.stockup.backend.domain.bharosa;

/**
 * A compact banner signal. {@code tone} maps onto the app's existing five
 * semantic colours, so the banner needs no legend.
 *
 * @param tone one of positive | attention | urgent | info | special
 */
public record BharosaTag(String icon, String label, String tone) {

    public static BharosaTag positive(String icon, String label) {
        return new BharosaTag(icon, label, "positive");
    }

    public static BharosaTag attention(String icon, String label) {
        return new BharosaTag(icon, label, "attention");
    }

    public static BharosaTag urgent(String icon, String label) {
        return new BharosaTag(icon, label, "urgent");
    }

    /** For facts that are not judgements — "new shop" is not a warning. */
    public static BharosaTag info(String icon, String label) {
        return new BharosaTag(icon, label, "info");
    }

    public static BharosaTag special(String icon, String label) {
        return new BharosaTag(icon, label, "special");
    }
}
