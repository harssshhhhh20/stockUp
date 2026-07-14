package com.stockup.backend.domain.broadcast.entity;


import com.stockup.backend.common.persistence.entity.AuditableEntity;
import com.stockup.backend.domain.broadcast.entity.enums.BroadcastRecipientStatus;
import com.stockup.backend.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "broadcast_recipients")
public class BroadcastRecipient extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "broadcast_id", nullable = false)
    private Broadcast broadcast;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BroadcastRecipientStatus status;

    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
}
