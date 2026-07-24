package com.stockup.backend.domain.reservation.service.impl;

import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.merchantoffer.entity.MerchantOffer;
import com.stockup.backend.domain.merchantoffer.repository.MerchantOfferRepository;
import com.stockup.backend.domain.reservation.dto.CancelReservationRequest;
import com.stockup.backend.domain.reservation.dto.CompleteReservationRequest;
import com.stockup.backend.domain.reservation.dto.ReservationResponse;
import com.stockup.backend.domain.reservation.entity.Reservation;
import com.stockup.backend.domain.reservation.exception.*;
import com.stockup.backend.domain.reservation.mapper.ReservationMapper;
import com.stockup.backend.domain.reservation.otp.ReservationOtpGenerator;
import com.stockup.backend.domain.reservation.repository.ReservationRepository;
import com.stockup.backend.domain.reservation.service.ReservationService;
import com.stockup.backend.domain.store.entity.Store;
import com.stockup.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final MerchantOfferRepository merchantOfferRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationOtpGenerator reservationOtpGenerator;

    @Override
    public ReservationResponse reserveMerchantOffer(UUID merchantOfferId) {

        MerchantOffer merchantOffer = merchantOfferRepository.findById(merchantOfferId)
                .orElseThrow(() -> new MerchantOfferNotFoundException(
                        "Merchant offer not found."
                ));

        if (reservationRepository.existsByMerchantOfferId(merchantOfferId)) {
            throw new ReservationAlreadyExistsException(
                    "A reservation already exists for this merchant offer."
            );
        }

        Basket basket = merchantOffer.getBroadcastRecipient()
                .getBroadcast()
                .getBasket();
        Store store = merchantOffer.getBroadcastRecipient().getStore();
        Merchant merchant = store.getMerchant();
        User customer = basket.getCustomer();

        Reservation reservation = Reservation.create(
                basket,
                merchantOffer,
                customer,
                merchant,
                store
        );

        reservationRepository.save(reservation);

        return reservationMapper.toResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservation(UUID reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found."
                ));

        return reservationMapper.toResponse(reservation);
    }

    @Override
    public ReservationResponse cancelByCustomer(
            UUID reservationId,
            CancelReservationRequest request
    ) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found."
                ));

        reservation.cancelByCustomer(request.reason());

        Reservation updatedReservation = reservationRepository.save(reservation);

        return reservationMapper.toResponse(updatedReservation);
    }

    @Override
    public ReservationResponse cancelByMerchant(
            UUID reservationId,
            CancelReservationRequest request
    ) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found."
                ));

        reservation.cancelByMerchant(request.reason());
        Reservation updatedReservation = reservationRepository.save(reservation);
        return reservationMapper.toResponse(updatedReservation);
    }

    @Override
    public void activateReservation(UUID reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found."
                ));
        String otp = reservationOtpGenerator.generate();
        reservation.activate(otp);

        reservationRepository.save(reservation);
    }

    @Override
    public void expireReservation(UUID reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found."
                ));

        reservation.expire();

        reservationRepository.save(reservation);
    }

    @Override
    public ReservationResponse completeReservation(
            UUID reservationId,
            CompleteReservationRequest request
    ) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found."
                ));

        reservation.complete(request.otp());

        Reservation updatedReservation = reservationRepository.save(reservation);

        return reservationMapper.toResponse(updatedReservation);
    }
}
