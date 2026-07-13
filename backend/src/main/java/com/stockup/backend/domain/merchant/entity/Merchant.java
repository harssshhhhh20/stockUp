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

    protected Merchant() {
    }

    public Merchant(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}