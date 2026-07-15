package com.stockup.backend.domain.broadcast.repository;

import com.stockup.backend.domain.broadcast.entity.BroadcastRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BroadcastRecipientRepository
        extends JpaRepository<BroadcastRecipient, UUID> {
}