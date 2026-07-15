package com.stockup.backend.domain.merchantoffer.service.impl;

import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.basket.entity.BasketItem;
import com.stockup.backend.domain.broadcast.entity.BroadcastRecipient;
import com.stockup.backend.domain.broadcast.repository.BroadcastRecipientRepository;
import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.merchant.repository.MerchantRepository;
import com.stockup.backend.domain.merchantoffer.dto.SubmitMerchantOfferRequest;
import com.stockup.backend.domain.merchantoffer.dto.SubmitMerchantOfferResponse;
import com.stockup.backend.domain.merchantoffer.entity.MerchantOffer;
import com.stockup.backend.domain.merchantoffer.mapper.MerchantOfferMapper;
import com.stockup.backend.domain.merchantoffer.repository.MerchantOfferRepository;
import com.stockup.backend.domain.merchantoffer.service.MerchantOfferService;
import com.stockup.backend.domain.merchantoffer.value.MerchantOfferResponse;
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

        return new SubmitMerchantOfferResponse(
                merchantOffer.getId()
        );

    }

}