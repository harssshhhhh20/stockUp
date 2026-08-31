package com.stockup.backend.domain.basket.scheduler;

import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.basket.enums.BasketStatus;
import com.stockup.backend.domain.basket.repository.BasketRepository;
import com.stockup.backend.domain.broadcast.entity.BroadcastRecipient;
import com.stockup.backend.domain.broadcast.entity.enums.BroadcastRecipientStatus;
import com.stockup.backend.domain.broadcast.repository.BroadcastRecipientRepository;
import com.stockup.backend.domain.broadcast.repository.BroadcastRepository;
import com.stockup.backend.domain.merchant.service.BharosaScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BasketExpiryScheduler {

    private final BasketRepository basketRepository;
    private final BroadcastRepository broadcastRepository;
    private final BroadcastRecipientRepository broadcastRecipientRepository;
    private final BharosaScoreService bharosaScoreService;

    @Transactional
    @Scheduled(fixedDelay = 60_000)
    public void expireBaskets() {

        List<Basket> expiredBaskets =
                basketRepository.findAllByStatusAndExpiresAtBefore(
                        BasketStatus.ACTIVE,
                        Instant.now()
                );

        for (Basket basket : expiredBaskets) {

            basket.expire();

            broadcastRepository.findByBasket(basket).ifPresent(broadcast -> {

                broadcast.expire();

                List<BroadcastRecipient> recipients =
                        broadcastRecipientRepository.findAllByBroadcast(broadcast);

                for (BroadcastRecipient recipient : recipients) {

                    if (recipient.getStatus() == BroadcastRecipientStatus.PENDING) {

                        bharosaScoreService.adjust(
                                recipient.getStore().getMerchant(),
                                BharosaScoreService.BROADCAST_NOT_VIEWED_DELTA,
                                "Broadcast " + broadcast.getId() + " expired without being opened."
                        );

                        recipient.expire();

                    } else if (recipient.getStatus() == BroadcastRecipientStatus.VIEWED) {

                        bharosaScoreService.adjust(
                                recipient.getStore().getMerchant(),
                                BharosaScoreService.BROADCAST_VIEWED_NOT_RESPONDED_DELTA,
                                "Broadcast " + broadcast.getId() + " was viewed but never responded to."
                        );

                        recipient.expire();
                    }
                }
            });
        }
    }
}