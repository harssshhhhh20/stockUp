package com.stockup.backend.domain.broadcast.service;

import com.stockup.backend.domain.basket.entity.Basket;

import java.util.UUID;

public interface BroadcastService {

    void broadcastBasket(Basket basket);

    void markViewed(UUID broadcastRecipientId);

}
