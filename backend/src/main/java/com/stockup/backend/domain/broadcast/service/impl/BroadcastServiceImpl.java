package com.stockup.backend.domain.broadcast.service.impl;

import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.basket.entity.BasketTargetStore;
import com.stockup.backend.domain.broadcast.entity.Broadcast;
import com.stockup.backend.domain.broadcast.entity.BroadcastRecipient;
import com.stockup.backend.domain.broadcast.exception.BroadcastAlreadyExistsException;
import com.stockup.backend.domain.broadcast.exception.NoTargetStoresFoundException;
import com.stockup.backend.domain.broadcast.repository.BroadcastRecipientRepository;
import com.stockup.backend.domain.broadcast.repository.BroadcastRepository;
import com.stockup.backend.domain.broadcast.service.BroadcastService;
import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.merchant.repository.MerchantRepository;
import com.stockup.backend.domain.store.entity.Store;
import com.stockup.backend.domain.store.repository.StoreRepository;
import com.stockup.backend.domain.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BroadcastServiceImpl implements BroadcastService {

    private final BroadcastRepository broadcastRepository;
    private final StoreRepository storeRepository;
    private final BroadcastRecipientRepository broadcastRecipientRepository;
    private final CurrentUserService currentUserService;
    private final MerchantRepository merchantRepository;

    @Override
    public void broadcastBasket(Basket basket) {

        if (broadcastRepository.existsByBasket(basket)) {
            throw new BroadcastAlreadyExistsException(
                    "Broadcast already exists for this basket."
            );
        }
        List<Store> stores = resolveTargetStores(basket);

        if (stores.isEmpty()) {
            throw new NoTargetStoresFoundException(
                    "No target stores found for basket " + basket.getId()
            );
        }
        Broadcast broadcast = Broadcast.create(basket);

        for (Store store : stores) {
            broadcast.addRecipient(store);
        }

        broadcastRepository.save(broadcast);
    }
    private List<Store> resolveTargetStores(Basket basket){
        return switch (basket.getTargetMode()) {

            case SELECTED_STORES -> basket.getTargetStores()
                    .stream()
                    .map(BasketTargetStore::getStore)
                    .toList();

            case NEARBY ->
                    storeRepository.findNearbyStores(
                            basket.getBasketLatitude(),
                            basket.getBasketLongitude(),
                            basket.getSearchRadiusMeters()
                    );
        };
    }

    @Override
    public void markViewed(UUID broadcastRecipientId) {

        BroadcastRecipient broadcastRecipient =
                broadcastRecipientRepository.findById(broadcastRecipientId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Broadcast recipient not found."
                        ));

        User currentUser = currentUserService.getCurrentUser();

        Merchant merchant = merchantRepository.findByUser(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Merchant not found for authenticated user."));

        if (!broadcastRecipient.getStore()
                .getMerchant()
                .getId()
                .equals(merchant.getId())) {

            throw new AccessDeniedException(
                    "You are not authorized to view this broadcast."
            );
        }

        broadcastRecipient.markViewed();
    }

}