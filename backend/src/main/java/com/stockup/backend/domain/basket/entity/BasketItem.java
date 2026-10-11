package com.stockup.backend.domain.basket.entity;


import com.stockup.backend.common.persistence.entity.AuditableEntity;
import com.stockup.backend.domain.basket.enums.BasketItemUnit;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "basket_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasketItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "basket_id", nullable = false)
    private Basket basket;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BasketItemUnit unit;

    @Column(length = 100)
    private String brand;

    @Column(length = 500)
    private String notes;

    /**
     * What the shopper expects to pay per unit, if they know.
     *
     * This is the shopper's number, not a catalogue price — it is what makes a
     * quote meaningful without StockUp ever having to maintain a price list.
     * Null whenever they don't know, which must stay perfectly acceptable.
     */
    @Column(name = "expected_unit_price", precision = 10, scale = 2)
    private BigDecimal expectedUnitPrice;

    /** What this line is expected to cost. Null unless a price was given. */
    public BigDecimal expectedLineTotal() {
        return expectedUnitPrice == null
                ? null
                : expectedUnitPrice.multiply(quantity);
    }
}