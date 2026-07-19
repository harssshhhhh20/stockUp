package com.stockup.backend.domain.broadcast.entity;


import com.stockup.backend.common.persistence.entity.AuditableEntity;
import com.stockup.backend.domain.broadcast.entity.enums.BroadcastRecipientStatus;
import com.stockup.backend.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

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
    private Instant viewedAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    public void markResponded() {

        if (this.status != BroadcastRecipientStatus.VIEWED) {
            throw new IllegalStateException(
                    "Only viewed broadcast recipients can be marked as responded."
            );
        }

        this.status = BroadcastRecipientStatus.RESPONDED;
        this.respondedAt = Instant.now();
    }
    public void markViewed() {
        if (status != BroadcastRecipientStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending broadcast recipients can be marked as viewed."
            );
        }

        this.status = BroadcastRecipientStatus.VIEWED;
        this.viewedAt = Instant.now();
    }
    public void expire() {
        if (status == BroadcastRecipientStatus.RESPONDED
                || status == BroadcastRecipientStatus.EXPIRED) {
            throw new IllegalStateException(
                    "Only pending or viewed broadcast recipients can be expired."
            );
        }

        this.status = BroadcastRecipientStatus.EXPIRED;
    }
}
