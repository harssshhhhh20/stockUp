package com.stockup.backend.domain.broadcast.service;

import com.stockup.backend.domain.basket.entity.Basket;

public interface BroadcastService {

    void broadcastBasket(Basket basket);

}
