package com.stockup.backend.domain.merchantoffer.repository;

import com.stockup.backend.domain.broadcast.entity.BroadcastRecipient;
import com.stockup.backend.domain.merchantoffer.entity.MerchantOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MerchantOfferRepository extends JpaRepository<MerchantOffer, UUID> {

    boolean existsByBroadcastRecipient(BroadcastRecipient broadcastRecipient);

    Optional<MerchantOffer> findByBroadcastRecipient(BroadcastRecipient broadcastRecipient);
}