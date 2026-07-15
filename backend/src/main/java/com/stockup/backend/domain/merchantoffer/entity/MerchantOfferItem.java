package com.stockup.backend.domain.merchantoffer.entity;

import com.stockup.backend.common.persistence.entity.AuditableEntity;
import com.stockup.backend.domain.basket.entity.BasketItem;
import com.stockup.backend.domain.merchantoffer.enums.MerchantOfferItemStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "merchant_offer_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MerchantOfferItem extends AuditableEntity {

    MerchantOfferItem(
            MerchantOffer merchantOffer,
            BasketItem basketItem,
            MerchantOfferItemStatus status,
            BigDecimal availableQuantity
    ) {
        this.merchantOffer = Objects.requireNonNull(merchantOffer);
        this.basketItem = Objects.requireNonNull(basketItem);
        this.status = Objects.requireNonNull(status);

        validateAvailableQuantity(status, availableQuantity);

        this.availableQuantity = availableQuantity;
    }
    private void validateAvailableQuantity(
            MerchantOfferItemStatus status,
            BigDecimal availableQuantity
    ) {

        switch (status) {

            case AVAILABLE, NOT_AVAILABLE -> {
                if (availableQuantity != null) {
                    throw new IllegalArgumentException(
                            "Available quantity must only be provided for PARTIAL responses."
                    );
                }
            }

            case PARTIAL -> {

                if (availableQuantity == null) {
                    throw new IllegalArgumentException(
                            "Available quantity is required for PARTIAL responses."
                    );
                }

                if (availableQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(
                            "Available quantity must be greater than zero."
                    );
                }

                if (availableQuantity.compareTo(basketItem.getQuantity()) > 0) {
                    throw new IllegalArgumentException(
                            "Available quantity cannot exceed requested quantity."
                    );
                }
            }
        }
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_offer_id", nullable = false)
    private MerchantOffer merchantOffer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "basket_item_id", nullable = false)
    private BasketItem basketItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MerchantOfferItemStatus status;

    @Column(name = "available_quantity")
    private BigDecimal availableQuantity;
}