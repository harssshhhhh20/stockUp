package com.stockup.backend.domain.basket.entity;


import com.stockup.backend.common.persistence.entity.AuditableEntity;
import com.stockup.backend.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "basket_selected_stores")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasketTargetStore extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "basket_id", nullable = false)
    private Basket basket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

}