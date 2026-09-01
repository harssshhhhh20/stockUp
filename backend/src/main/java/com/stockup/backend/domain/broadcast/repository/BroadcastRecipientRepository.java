package com.stockup.backend.domain.broadcast.repository;

import com.stockup.backend.domain.broadcast.entity.Broadcast;
import com.stockup.backend.domain.broadcast.entity.BroadcastRecipient;
import com.stockup.backend.domain.broadcast.entity.enums.BroadcastRecipientStatus;
import com.stockup.backend.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BroadcastRecipientRepository
        extends JpaRepository<BroadcastRecipient, UUID> {
    List<BroadcastRecipient> findAllByBroadcast(Broadcast broadcast);

    List<BroadcastRecipient> findAllByStoreAndStatusInOrderByCreatedAtDesc(
            Store store,
            List<BroadcastRecipientStatus> statuses
    );
}