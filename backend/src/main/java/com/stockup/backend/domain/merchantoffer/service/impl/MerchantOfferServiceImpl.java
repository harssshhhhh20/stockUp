package com.stockup.backend.domain.merchantoffer.service.impl;

import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.basket.entity.BasketItem;
import com.stockup.backend.domain.basket.repository.BasketRepository;
import com.stockup.backend.domain.broadcast.entity.BroadcastRecipient;
import com.stockup.backend.domain.broadcast.entity.enums.BroadcastRecipientStatus;
import com.stockup.backend.domain.broadcast.exception.BroadcastRecipientNotViewedException;
import com.stockup.backend.domain.broadcast.repository.BroadcastRecipientRepository;
import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.merchant.repository.MerchantRepository;
import com.stockup.backend.domain.merchant.service.BharosaScoreService;
import com.stockup.backend.domain.merchantoffer.dto.MerchantOfferItemSummaryResponse;
import com.stockup.backend.domain.merchantoffer.dto.MerchantOfferSummaryResponse;
import com.stockup.backend.domain.merchantoffer.dto.SubmitMerchantOfferRequest;
import com.stockup.backend.domain.merchantoffer.dto.SubmitMerchantOfferResponse;
import com.stockup.backend.domain.merchantoffer.entity.MerchantOffer;
import com.stockup.backend.domain.merchantoffer.mapper.MerchantOfferMapper;
import com.stockup.backend.domain.merchantoffer.repository.MerchantOfferRepository;
import com.stockup.backend.domain.merchantoffer.service.MerchantOfferService;
import com.stockup.backend.domain.merchantoffer.value.MerchantOfferResponse;
import com.stockup.backend.domain.notification.entity.enums.NotificationType;
import com.stockup.backend.domain.notification.service.NotificationService;
import com.stockup.backend.domain.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MerchantOfferServiceImpl implements MerchantOfferService {

    private final BroadcastRecipientRepository broadcastRecipientRepository;
    private final MerchantOfferRepository merchantOfferRepository;
    private final MerchantOfferMapper merchantOfferMapper;
    private final CurrentUserService currentUserService;
    private final MerchantRepository merchantRepository;
    private final NotificationService notificationService;
    private final BasketRepository basketRepository;
    private final BharosaScoreService bharosaScoreService;

    @Override
    public SubmitMerchantOfferResponse submit(SubmitMerchantOfferRequest request) {

        BroadcastRecipient broadcastRecipient =
                broadcastRecipientRepository.findById(request.broadcastRecipientId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Broadcast recipient not found."
                        ));
        User currentUser = currentUserService.getCurrentUser();

        Merchant merchant = merchantRepository.findByUser(currentUser)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Merchant not found for authenticated user."
                ));

        if (!broadcastRecipient.getStore()
                .getMerchant()
                .getId()
                .equals(merchant.getId())) {

            throw new AccessDeniedException(
                    "You are not authorized to submit an offer for this broadcast."
            );
        }

        if (merchantOfferRepository.existsByBroadcastRecipient(broadcastRecipient)) {
            throw new IllegalArgumentException(
                    "Merchant has already submitted an offer for this broadcast."
            );
        }

        if (broadcastRecipient.getStatus() != BroadcastRecipientStatus.VIEWED) {
            throw new BroadcastRecipientNotViewedException(
                    "Mark this broadcast as viewed before submitting an offer."
            );
        }
        Map<UUID, BasketItem> basketItems = broadcastRecipient
                .getBroadcast()
                .getBasket()
                .getItems()
                .stream()
                .collect(Collectors.toMap(
                        BasketItem::getId,
                        Function.identity()
                ));
        List<MerchantOfferResponse> responses = merchantOfferMapper.toResponses(request, basketItems);
        MerchantOffer merchantOffer = MerchantOffer.submit(
                broadcastRecipient,
                responses
        );
        broadcastRecipient.markResponded();

        merchantOfferRepository.save(merchantOffer);

        bharosaScoreService.adjust(
                merchant,
                BharosaScoreService.BROADCAST_RESPONDED_DELTA,
                "Merchant responded to broadcast " + broadcastRecipient.getBroadcast().getId() + "."
        );

        notificationService.notify(
                broadcastRecipient.getBroadcast().getBasket().getCustomer(),
                NotificationType.OFFER_SUBMITTED,
                "New offer on your basket",
                "A nearby store responded to your basket request.",
                merchantOffer.getId()
        );

        return new SubmitMerchantOfferResponse(
                merchantOffer.getId()
        );

    }

    @Override
    @Transactional
    public List<MerchantOfferSummaryResponse> getOffersForBasket(UUID basketId) {

        User currentUser = currentUserService.getCurrentUser();

        Basket basket = basketRepository.findByIdAndCustomer(basketId, currentUser)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Basket not found."
                ));

        return merchantOfferRepository
                .findAllByBroadcastRecipient_Broadcast_Basket(basket)
                .stream()
                .map(offer -> new MerchantOfferSummaryResponse(
                        offer.getId(),
                        offer.getBroadcastRecipient().getId(),
                        offer.getBroadcastRecipient().getStore().getId(),
                        offer.getBroadcastRecipient().getStore().getName(),
                        offer.getStatus(),
                        offer.getCreatedAt(),
                        offer.getOfferItems()
                                .stream()
                                .map(item -> new MerchantOfferItemSummaryResponse(
                                        item.getBasketItem().getId(),
                                        item.getBasketItem().getProductName(),
                                        item.getStatus(),
                                        item.getAvailableQuantity()
                                ))
                                .toList()
                ))
                .toList();
    }

}