package com.stockup.backend.infrastructure.notification.push;

import com.stockup.backend.common.persistence.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "push_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushToken extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(length = 20)
    private String platform;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    private PushToken(UUID userId, String token, String platform) {
        this.userId = userId;
        this.token = token;
        this.platform = platform;
    }

    public static PushToken of(UUID userId, String token, String platform) {
        return new PushToken(userId, token, platform);
    }

    public void markUsed() {
        this.lastUsedAt = Instant.now();
    }

    /** Expo told us this device is gone; stop pushing to it. */
    public void deactivate() {
        this.active = false;
    }

    public void reactivateFor(UUID userId) {
        this.userId = userId;
        this.active = true;
    }
}
