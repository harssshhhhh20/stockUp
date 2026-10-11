package com.stockup.backend.domain.pricing.entity;

import com.stockup.backend.common.persistence.entity.AuditableEntity;
import com.stockup.backend.domain.basket.enums.BasketItemUnit;
import com.stockup.backend.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

/**
 * What this shop last charged for this thing.
 *
 * Written as a side effect of replying, never entered by hand. It exists so the
 * shopkeeper's second reply is faster than his first and his twentieth is three
 * taps — which is the only way per-item prices survive contact with a shop
 * during evening rush.
 */
@Entity
@Table(name = "store_item_prices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreItemPrice extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false, updatable = false)
    private Store store;

    @Column(name = "product_key", nullable = false, length = 160, updatable = false)
    private String productKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, updatable = false)
    private BasketItemUnit unit;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /** How often this shop has stood behind this price — confidence, roughly. */
    @Column(name = "times_quoted", nullable = false)
    private int timesQuoted;

    private StoreItemPrice(Store store, String productKey, BasketItemUnit unit, BigDecimal unitPrice) {
        this.store = Objects.requireNonNull(store);
        this.productKey = Objects.requireNonNull(productKey);
        this.unit = Objects.requireNonNull(unit);
        this.unitPrice = Objects.requireNonNull(unitPrice);
        this.timesQuoted = 1;
    }

    public static StoreItemPrice first(
            Store store, String productName, BasketItemUnit unit, BigDecimal unitPrice) {
        return new StoreItemPrice(store, key(productName), unit, unitPrice);
    }

    /** Latest quote wins; a shop's price today matters more than last month's. */
    public void remember(BigDecimal unitPrice) {
        this.unitPrice = Objects.requireNonNull(unitPrice);
        this.timesQuoted++;
    }

    /**
     * Collapses free-text product names onto one row.
     *
     * StockUp has no catalogue — shoppers type whatever they call the thing —
     * so "Toor Dal  1kg", "toor dal 1kg" and "Toor-Dal 1Kg" have to agree or
     * the memory never warms up and the shopkeeper keeps typing.
     */
    public static String key(String productName) {
        String normalised = Normalizer
                .normalize(productName == null ? "" : productName, Normalizer.Form.NFKD)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
        return normalised.length() > 160 ? normalised.substring(0, 160) : normalised;
    }
}
