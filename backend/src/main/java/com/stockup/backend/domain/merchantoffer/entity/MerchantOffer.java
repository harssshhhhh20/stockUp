package com.stockup.backend.domain.merchantoffer.entity;

import com.stockup.backend.common.persistence.entity.AuditableEntity;
import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.basket.entity.BasketItem;
import com.stockup.backend.domain.broadcast.entity.BroadcastRecipient;
import com.stockup.backend.domain.broadcast.entity.enums.BroadcastRecipientStatus;
import com.stockup.backend.domain.merchantoffer.enums.MerchantOfferStatus;

import com.stockup.backend.domain.merchantoffer.value.MerchantOfferResponse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "merchant_offers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MerchantOffer extends AuditableEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "broadcast_recipient_id", nullable = false, unique = true)
    private BroadcastRecipient broadcastRecipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MerchantOfferStatus status;

    @OneToMany(
            mappedBy = "merchantOffer",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private final List<MerchantOfferItem> offerItems = new ArrayList<>();

    private MerchantOffer(BroadcastRecipient broadcastRecipient) {
        this.broadcastRecipient = Objects.requireNonNull(broadcastRecipient);
        this.status = MerchantOfferStatus.SUBMITTED;
    }

    public static MerchantOffer submit(
            BroadcastRecipient broadcastRecipient,
            List<MerchantOfferResponse> responses
    ) {
        Objects.requireNonNull(responses);
        MerchantOffer offer = new MerchantOffer(broadcastRecipient);
        Basket basket = broadcastRecipient.getBroadcast().getBasket();

        if (responses.size() != basket.getItems().size()) {
            throw new IllegalArgumentException(
                    "Every basket item must have exactly one response."
            );
        }
        Set<BasketItem> respondedItems = responses.stream()
                .map(MerchantOfferResponse::basketItem)
                .collect(Collectors.toSet());
        if (respondedItems.size() != responses.size()) {
            throw new IllegalArgumentException(
                    "Duplicate responses for basket items are not allowed."
            );
        }
        for (MerchantOfferResponse response : responses) {
            if (!basket.getItems().contains(response.basketItem())) {
                throw new IllegalArgumentException(
                        "Response contains a basket item that does not belong to the basket."
                );
            }
            offer.offerItems.add(
                    new MerchantOfferItem(
                            offer,
                            response.basketItem(),
                            response.status(),
                            response.availableQuantity()
                    )
            );
        }
        return offer;
    }
}