package com.stockup.backend.domain.reservation.service.impl;


import com.stockup.backend.common.exceptions.model.ResourceNotFoundException;
import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.broadcast.entity.Broadcast;
import com.stockup.backend.domain.broadcast.entity.BroadcastRecipient;
import com.stockup.backend.domain.merchantoffer.entity.MerchantOffer;
import com.stockup.backend.domain.merchantoffer.repository.MerchantOfferRepository;
import com.stockup.backend.domain.reservation.dto.CreateReservationRequest;
import com.stockup.backend.domain.reservation.dto.CreateReservationResponse;
import com.stockup.backend.domain.reservation.dto.ReservationDetailResponse;
import com.stockup.backend.domain.reservation.dto.ReservationSummaryResponse;
import com.stockup.backend.domain.reservation.exception.BusinessException;
import com.stockup.backend.domain.reservation.mapper.ReservationMapper;
import com.stockup.backend.domain.reservation.model.Reservation;
import com.stockup.backend.domain.reservation.model.ReservationStatus;
import com.stockup.backend.domain.reservation.repository.ReservationRepository;
import com.stockup.backend.domain.reservation.service.ReservationService;
import com.stockup.backend.domain.store.entity.Store;
import com.stockup.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final MerchantOfferRepository merchantOfferRepository;
    private final CurrentUserService currentUserService;
    private final ReservationMapper reservationMapper;

    @Override
    public CreateReservationResponse create(CreateReservationRequest request) {

        MerchantOffer merchantOffer = merchantOfferRepository.findById(request.merchantOfferId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Merchant offer not found."
                ));

        User currentUser = currentUserService.getCurrentUser();

        BroadcastRecipient broadcastRecipient = merchantOffer.getBroadcastRecipient();
        Broadcast broadcast = broadcastRecipient.getBroadcast();
        Basket basket = broadcast.getBasket();
        Store store = broadcastRecipient.getStore();

        if (!basket.getCustomer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "You are not authorized to create a reservation for this merchant offer."
            );
        }

        if (reservationRepository.existsByBasketAndStatus(
                basket,
                ReservationStatus.CREATED
        )) {
            throw new BusinessException("Already active Reservation");
        }

        merchantOffer.markReserved();

        Instant reservedUntil = Instant.now().plus(Duration.ofMinutes(30));

        Reservation reservation = Reservation.create(
                basket,
                merchantOffer,
                currentUser,
                store,
                reservedUntil
        );

        List<MerchantOffer> merchantOffers =
                merchantOfferRepository.findAllByBroadcastRecipient_Broadcast(broadcast);

        for (MerchantOffer offer : merchantOffers) {
            if (!offer.getId().equals(merchantOffer.getId())) {
                offer.markNotSelected();
            }
        }

        broadcast.complete();
        basket.reserve();
        reservationRepository.save(reservation);
        return new CreateReservationResponse(reservation.getId());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReservationSummaryResponse> getReservations() {

        User customer = currentUserService.getCurrentUser();

        return reservationRepository
                .findAllByBasketCustomerOrderByCreatedAtDesc(customer)
                .stream()
                .map(reservationMapper::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public ReservationDetailResponse getReservation(UUID reservationId) {

        User customer = currentUserService.getCurrentUser();

        Reservation reservation = reservationRepository
                .findByIdAndCustomer(reservationId, customer)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        return reservationMapper.toDetailResponse(reservation);
    }
}