package com.stockup.backend.domain.broadcast.service.impl;

import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.basket.entity.BasketTargetStore;
import com.stockup.backend.domain.broadcast.dto.BroadcastRecipientSummaryResponse;
import com.stockup.backend.domain.broadcast.entity.Broadcast;
import com.stockup.backend.domain.broadcast.entity.BroadcastRecipient;
import com.stockup.backend.domain.broadcast.entity.enums.BroadcastRecipientStatus;
import com.stockup.backend.domain.broadcast.exception.BroadcastAlreadyExistsException;
import com.stockup.backend.domain.broadcast.exception.NoTargetStoresFoundException;
import com.stockup.backend.domain.broadcast.repository.BroadcastRecipientRepository;
import com.stockup.backend.domain.broadcast.repository.BroadcastRepository;
import com.stockup.backend.domain.broadcast.service.BroadcastService;
import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.merchant.repository.MerchantRepository;
import com.stockup.backend.domain.notification.entity.enums.NotificationType;
import com.stockup.backend.domain.notification.service.NotificationService;
import com.stockup.backend.domain.reservation.event.EventActor;
import com.stockup.backend.domain.reservation.event.ReservationEventRecorder;
import com.stockup.backend.domain.reservation.event.ReservationEventType;
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
    private final NotificationService notificationService;
    private final ReservationEventRecorder eventRecorder;

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

        for (Store store : stores) {
            eventRecorder.recordBroadcastStage(
                    basket.getId(), store.getId(), store.getMerchant().getId(),
                    ReservationEventType.REQUEST_BROADCAST, EventActor.SYSTEM);

            notificationService.notify(
                    store.getMerchant().getUser(),
                    NotificationType.BASKET_BROADCASTED,
                    "New nearby basket",
                    "A customer nearby is looking for items your store may have.",
                    basket.getId()
            );
        }
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

        eventRecorder.recordBroadcastStage(
                broadcastRecipient.getBroadcast().getBasket().getId(),
                broadcastRecipient.getStore().getId(),
                merchant.getId(),
                ReservationEventType.MERCHANT_VIEWED, EventActor.MERCHANT);
    }

    @Override
    public List<BroadcastRecipientSummaryResponse> getMyPendingBroadcasts() {

        User currentUser = currentUserService.getCurrentUser();

        Merchant merchant = merchantRepository.findByUser(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Merchant not found for authenticated user."));

        Store store = storeRepository.findByMerchant(merchant)
                .orElseThrow(() -> new EntityNotFoundException("Store not found for authenticated merchant."));

        List<BroadcastRecipient> recipients = broadcastRecipientRepository
                .findAllByStoreAndStatusInOrderByCreatedAtDesc(
                        store,
                        List.of(BroadcastRecipientStatus.PENDING, BroadcastRecipientStatus.VIEWED)
                );

        return recipients.stream()
                .map(recipient -> {
                    Basket basket = recipient.getBroadcast().getBasket();
                    return new BroadcastRecipientSummaryResponse(
                            recipient.getId(),
                            basket.getId(),
                            recipient.getStatus(),
                            recipient.getCreatedAt(),
                            recipient.getViewedAt(),
                            basket.getExpiresAt(),
                            basket.getItems().stream()
                                    .map(item -> new BroadcastRecipientSummaryResponse.Item(
                                            item.getId(),
                                            item.getProductName(),
                                            item.getQuantity(),
                                            item.getUnit(),
                                            item.getBrand(),
                                            item.getNotes()
                                    ))
                                    .toList()
                    );
                })
                .toList();
    }

}