package com.stockup.backend.domain.basket.service.impl;

import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.basket.repository.BasketRepository;
import com.stockup.backend.domain.basket.service.BasketPublishingService;
import com.stockup.backend.domain.broadcast.service.BroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class BasketPublishingServiceImpl implements BasketPublishingService {

    private final BroadcastService broadcastService;
    private final BasketRepository basketRepository;

    @Override
    public void publish(Basket basket) {
        broadcastService.broadcastBasket(basket);
        basket.publish();
        basketRepository.save(basket);
    }
}