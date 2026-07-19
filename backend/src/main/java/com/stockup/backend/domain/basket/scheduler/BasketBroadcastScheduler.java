package com.stockup.backend.domain.basket.scheduler;

import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.basket.enums.BasketStatus;
import com.stockup.backend.domain.basket.repository.BasketRepository;
import com.stockup.backend.domain.basket.service.BasketPublishingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BasketBroadcastScheduler {

    private final BasketRepository basketRepository;
    private final BasketPublishingService basketPublishingService;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void broadcastPendingBaskets() {
        List<Basket> baskets =
                basketRepository.findByStatusAndBroadcastAtLessThanEqual(
                        BasketStatus.PENDING_BROADCAST,
                        Instant.now()
                );
        if (!baskets.isEmpty()) {
            for (Basket basket : baskets) {
                basketPublishingService.publish(basket);
            }

        }
    }

}
