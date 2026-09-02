package com.stockup.backend.domain.merchant.entity;

import com.stockup.backend.common.persistence.entity.AuditableEntity;
import com.stockup.backend.domain.user.entity.User;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "merchants")
public class Merchant extends AuditableEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Trust score used to gate/flag merchant behaviour (cancellations, no-shows).
     * Starts at 100; adjusted by {@link com.stockup.backend.domain.merchant.service.BharosaScoreService}.
     */
    @Column(name = "bharosa_score", nullable = false)
    private int bharosaScore = 100;

    protected Merchant() {
    }

    public Merchant(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public int getBharosaScore() {
        return bharosaScore;
    }

    /**
     * Replaces the score outright — used by the Kasauti engine, which derives the
     * whole value from the event log rather than nudging the previous one.
     */
    public void setBharosaScore(int score) {
        this.bharosaScore = Math.max(0, Math.min(100, score));
    }

    public void adjustBharosaScore(int delta) {
        this.bharosaScore = Math.max(0, Math.min(100, this.bharosaScore + delta));
    }
}