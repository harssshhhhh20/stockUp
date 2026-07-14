package com.stockup.backend.domain.broadcast.service.impl;

import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.basket.entity.BasketTargetStore;
import com.stockup.backend.domain.broadcast.entity.Broadcast;
import com.stockup.backend.domain.broadcast.exception.BroadcastAlreadyExistsException;
import com.stockup.backend.domain.broadcast.repository.BroadcastRepository;
import com.stockup.backend.domain.broadcast.service.BroadcastService;
import com.stockup.backend.domain.store.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BroadcastServiceImpl implements BroadcastService {

    private final BroadcastRepository broadcastRepository;

    @Override
    public void broadcastBasket(Basket basket) {

        if (broadcastRepository.existsByBasket(basket)) {
            throw new BroadcastAlreadyExistsException(
                    "Broadcast already exists for this basket."
            );
        }

        Broadcast broadcast = Broadcast.create(basket);

        List<Store> stores = switch (basket.getTargetMode()) {

            case SELECTED_STORES -> basket.getTargetStores()
                    .stream()
                    .map(BasketTargetStore::getStore)
                    .toList();

            case NEARBY -> throw new UnsupportedOperationException(
                    "Nearby broadcast not implemented yet."
            );
        };

        for (Store store : stores) {
            broadcast.addRecipient(store);
        }

        broadcastRepository.save(broadcast);
    }
}