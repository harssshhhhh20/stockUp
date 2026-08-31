package com.stockup.backend.domain.notification.entity;

import com.stockup.backend.common.persistence.entity.AuditableEntity;
import com.stockup.backend.domain.notification.entity.enums.NotificationType;
import com.stockup.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(nullable = false)
    private boolean read = false;

    @Column(name = "read_at")
    private Instant readAt;

    private Notification(
            User recipient,
            NotificationType type,
            String title,
            String message,
            UUID referenceId
    ) {
        this.recipient = recipient;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
    }

    public static Notification create(
            User recipient,
            NotificationType type,
            String title,
            String message,
            UUID referenceId
    ) {
        return new Notification(recipient, type, title, message, referenceId);
    }

    public void markRead() {
        if (read) {
            return;
        }
        this.read = true;
        this.readAt = Instant.now();
    }
}
