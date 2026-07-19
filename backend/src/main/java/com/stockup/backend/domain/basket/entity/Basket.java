package com.stockup.backend.domain.basket.entity;

import com.stockup.backend.common.persistence.entity.AuditableEntity;
import com.stockup.backend.domain.basket.enums.BasketItemUnit;
import com.stockup.backend.domain.basket.enums.BasketStatus;
import com.stockup.backend.domain.basket.enums.BasketTargetMode;
import com.stockup.backend.domain.store.entity.Store;
import com.stockup.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "baskets")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Basket extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_mode", nullable = false)
    private BasketTargetMode targetMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BasketStatus status;

    @Column(name = "search_radius")
    private Integer searchRadiusMeters;

    @Column(name = "basket_latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal basketLatitude;

    @Column(name = "basket_longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal basketLongitude;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "broadcast_at", nullable = false)
    private Instant broadcastAt;

    @OneToMany(
            mappedBy = "basket",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<BasketItem> items = new ArrayList<>();

    @OneToMany(
            mappedBy = "basket",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<BasketTargetStore> targetStores = new ArrayList<>();

    public void addItem(
            String productName,
            BigDecimal quantity,
            BasketItemUnit unit,
            String brand,
            String notes
    ) {
        BasketItem item = BasketItem.builder()
                .basket(this)
                .productName(productName)
                .quantity(quantity)
                .unit(unit)
                .brand(brand)
                .notes(notes)
                .build();

        items.add(item);
    }


    public void addTargetStore(Store store) {
        BasketTargetStore targetStore = BasketTargetStore.builder()
                .basket(this)
                .store(store)
                .build();

        targetStores.add(targetStore);
    }

    public static Basket create(
            User customer,
            BasketTargetMode targetMode,
            Integer searchRadiusMeters,
            BigDecimal basketLatitude,
            BigDecimal basketLongitude
    ) {
        Instant broadcastAt = Instant.now().plusSeconds(10);

        return Basket.builder()
                .customer(customer)
                .targetMode(targetMode)
                .status(BasketStatus.PENDING_BROADCAST)
                .broadcastAt(broadcastAt)
                .searchRadiusMeters(searchRadiusMeters)
                .basketLatitude(basketLatitude)
                .basketLongitude(basketLongitude)
                .expiresAt(broadcastAt.plus(15, ChronoUnit.MINUTES))
                .build();
    }

    public void reserve() {
        requireStatus(BasketStatus.ACTIVE);
        this.status = BasketStatus.RESERVED;
    }
    public void expire() {
        requireStatus(BasketStatus.ACTIVE);
        this.status = BasketStatus.EXPIRED;
    }

    public void cancel() {
        requireStatus(BasketStatus.ACTIVE);
        this.status = BasketStatus.CANCELLED;
    }
    public void publish() {
        requireStatus(BasketStatus.PENDING_BROADCAST);
        this.status = BasketStatus.ACTIVE;
    }
    private void requireStatus(BasketStatus expectedStatus) {
        if (status != expectedStatus) {
            throw new IllegalStateException(
                    "Expected basket status " + expectedStatus + " but was " + status + "."
            );
        }
    }
}