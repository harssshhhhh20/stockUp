package com.stockup.backend.domain.basket.service;

import com.stockup.backend.domain.basket.entity.Basket;

public interface BasketPublishingService {

    void publish(Basket basket);

}