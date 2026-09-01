package com.stockup.backend.domain.broadcast.service;

import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.broadcast.dto.BroadcastRecipientSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface BroadcastService {

    void broadcastBasket(Basket basket);

    void markViewed(UUID broadcastRecipientId);

    List<BroadcastRecipientSummaryResponse> getMyPendingBroadcasts();

}
