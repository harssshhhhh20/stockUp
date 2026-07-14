package com.stockup.backend.domain.broadcast.entity;


import com.stockup.backend.common.persistence.entity.AuditableEntity;
import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.broadcast.entity.enums.BroadcastRecipientStatus;
import com.stockup.backend.domain.broadcast.entity.enums.BroadcastStatus;
import com.stockup.backend.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "broadcasts")
public class Broadcast extends AuditableEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "basket_id", nullable = false, unique = true)
    private Basket basket;

    @OneToMany(
            mappedBy = "broadcast",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<BroadcastRecipient> recipients = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BroadcastStatus status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public void addRecipient(Store store) {

        BroadcastRecipient recipient = BroadcastRecipient.builder()
                .broadcast(this)
                .store(store)
                .status(BroadcastRecipientStatus.PENDING)
                .build();

        recipients.add(recipient);
    }


    public static Broadcast create(Basket basket) {

        return Broadcast.builder()
                .basket(basket)
                .status(BroadcastStatus.IN_PROGRESS)
                .build();
    }

}
