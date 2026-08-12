package com.stockup.backend.domain.reservation.service.impl;

import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.merchant.exception.MerchantNotFoundException;
import com.stockup.backend.domain.merchant.repository.MerchantRepository;
import com.stockup.backend.domain.merchantoffer.entity.MerchantOffer;
import com.stockup.backend.domain.merchantoffer.repository.MerchantOfferRepository;
import com.stockup.backend.domain.reservation.dto.CancelReservationRequest;
import com.stockup.backend.domain.reservation.dto.CompleteReservationRequest;
import com.stockup.backend.domain.reservation.dto.ReservationResponse;
import com.stockup.backend.domain.reservation.entity.Reservation;
import com.stockup.backend.domain.reservation.entity.enums.ReservationStatus;
import com.stockup.backend.domain.reservation.exception.*;
import com.stockup.backend.domain.reservation.mapper.ReservationMapper;
import com.stockup.backend.domain.reservation.otp.ReservationOtpGenerator;
import com.stockup.backend.domain.reservation.repository.ReservationRepository;
import com.stockup.backend.domain.reservation.service.ReservationService;
import com.stockup.backend.domain.store.entity.Store;
import com.stockup.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final MerchantOfferRepository merchantOfferRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationOtpGenerator reservationOtpGenerator;
    private final CurrentUserService currentUserService;
    private final MerchantRepository merchantRepository;

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

        User currentUser = currentUserService.getCurrentUser();

        Merchant merchant = merchantRepository.findByUser(currentUser)
                .orElseThrow(() -> new MerchantNotFoundException());
        reservation.validateMerchant(merchant);


        reservation.complete(request.otp());

        Reservation updatedReservation = reservationRepository.save(reservation);

        return reservationMapper.toResponse(updatedReservation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservations(
            ReservationStatus status,
            Pageable pageable
    ) {

        User currentUser = currentUserService.getCurrentUser();

        Optional<Merchant> merchant =
                merchantRepository.findByUser(currentUser);

        ReservationStatus effectiveStatus =
                status == null ? ReservationStatus.ACTIVE : status;

        if (merchant.isPresent()) {
            Page<Reservation> reservations =
                    reservationRepository.findByMerchantAndStatus(
                            merchant.get(),
                            effectiveStatus,
                            pageable
                    );

            return reservations.map(reservationMapper::toResponse);
        }
        Page<Reservation> reservations =
                reservationRepository.findByCustomerAndStatus(
                        currentUser,
                        effectiveStatus,
                        pageable
                );

        return reservations.map(reservationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservation(UUID reservationId){
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                () -> new ReservationNotFoundException("No reservation under this id")
        );
        User currUser = currentUserService.getCurrentUser();
        Optional<Merchant> merchant = merchantRepository.findByUser(currUser);

        if (merchant.isPresent()) {
            reservation.validateMerchant(merchant.get());
        } else {
            reservation.validateCustomer(currUser);
        }

        return reservationMapper.toResponse(reservation);

    }

    @Override
    public ReservationResponse cancelReservation(
            UUID reservationId,
            CancelReservationRequest request
    ){
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation Not found"));
        User currUser = currentUserService.getCurrentUser();
        Optional<Merchant> merchant = merchantRepository.findByUser(currUser);

        if(merchant.isPresent()){
            reservation.validateMerchant(merchant.get());
            reservation.cancelByMerchant(request.reason());
        }else{
            reservation.validateCustomer(currUser);
            reservation.cancelByCustomer(request.reason());
        }
        Reservation updatedReservation = reservationRepository.save(reservation);
        return reservationMapper.toResponse(updatedReservation);
    }
}
