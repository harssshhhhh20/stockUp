package com.stockup.backend.domain.reservation.service.impl;

import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.merchant.exception.MerchantNotFoundException;
import com.stockup.backend.domain.merchant.repository.MerchantRepository;
import com.stockup.backend.domain.merchant.service.BharosaScoreService;
import com.stockup.backend.domain.merchantoffer.entity.MerchantOffer;
import com.stockup.backend.domain.merchantoffer.repository.MerchantOfferRepository;
import com.stockup.backend.domain.notification.entity.enums.NotificationType;
import com.stockup.backend.domain.notification.service.NotificationService;
import com.stockup.backend.domain.reservation.dto.CancelReservationRequest;
import com.stockup.backend.domain.reservation.dto.CompleteReservationRequest;
import com.stockup.backend.domain.reservation.dto.ReservationResponse;
import com.stockup.backend.domain.reservation.entity.Reservation;
import com.stockup.backend.domain.reservation.entity.enums.ReservationStatus;
import com.stockup.backend.domain.reservation.event.EventActor;
import com.stockup.backend.domain.reservation.event.ReservationEventRecorder;
import com.stockup.backend.domain.reservation.event.ReservationEventType;
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
    private final NotificationService notificationService;
    private final BharosaScoreService bharosaScoreService;
    private final ReservationEventRecorder eventRecorder;

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

        eventRecorder.recordReservationStage(
                reservation, ReservationEventType.CUSTOMER_RESERVED, EventActor.CUSTOMER, null);

        notificationService.notify(
                merchant.getUser(),
                NotificationType.RESERVATION_CREATED,
                "New reservation request",
                "A customer has reserved your offer for one of their baskets.",
                reservation.getId()
        );

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

        eventRecorder.recordReservationStage(
                reservation, ReservationEventType.RESERVATION_ACTIVATED, EventActor.SYSTEM, null);

        notificationService.notify(
                reservation.getMerchant().getUser(),
                NotificationType.RESERVATION_ACTIVATED,
                "Reservation is now active",
                "The reservation is confirmed. Ask the customer for their handover OTP.",
                reservation.getId()
        );

        notificationService.notify(
                reservation.getCustomer(),
                NotificationType.RESERVATION_ACTIVATED,
                "Your reservation is active",
                "Your OTP is " + otp + ". Show it to the merchant to collect your order.",
                reservation.getId()
        );
    }

    @Override
    public void expireReservation(UUID reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found."
                ));

        reservation.expire();

        reservationRepository.save(reservation);

        eventRecorder.recordReservationStage(
                reservation, ReservationEventType.RESERVATION_EXPIRED, EventActor.SYSTEM, null);

        bharosaScoreService.adjust(
                reservation.getMerchant(),
                BharosaScoreService.MERCHANT_NO_SHOW_DELTA,
                "Reservation " + reservation.getId() + " expired without being fulfilled."
        );

        notificationService.notify(
                reservation.getCustomer(),
                NotificationType.RESERVATION_EXPIRED,
                "Reservation expired",
                "Your reservation was not fulfilled in time and has expired.",
                reservation.getId()
        );

        notificationService.notify(
                reservation.getMerchant().getUser(),
                NotificationType.RESERVATION_EXPIRED,
                "Reservation expired",
                "A reservation expired without being fulfilled. Your Bharosa Score has been affected.",
                reservation.getId()
        );
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

        eventRecorder.recordReservationStage(
                reservation, ReservationEventType.HANDOVER_COMPLETED, EventActor.MERCHANT, null);

        bharosaScoreService.adjust(
                merchant,
                BharosaScoreService.RESERVATION_COMPLETED_DELTA,
                "Reservation " + reservation.getId() + " completed successfully."
        );

        notificationService.notify(
                reservation.getCustomer(),
                NotificationType.RESERVATION_COMPLETED,
                "Order collected",
                "Your reservation has been completed. Enjoy!",
                reservation.getId()
        );

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

            eventRecorder.recordReservationStage(
                    reservation, ReservationEventType.MERCHANT_CANCELLED, EventActor.MERCHANT, null);

            bharosaScoreService.adjust(
                    merchant.get(),
                    BharosaScoreService.MERCHANT_CANCELLED_DELTA,
                    "Merchant cancelled reservation " + reservation.getId() + ": " + request.reason()
            );

            notificationService.notify(
                    reservation.getCustomer(),
                    NotificationType.RESERVATION_CANCELLED,
                    "Reservation cancelled by merchant",
                    "The merchant cancelled your reservation: " + request.reason(),
                    reservation.getId()
            );
        }else{
            reservation.validateCustomer(currUser);
            reservation.cancelByCustomer(request.reason());

            eventRecorder.recordReservationStage(
                    reservation, ReservationEventType.CUSTOMER_CANCELLED, EventActor.CUSTOMER, null);

            notificationService.notify(
                    reservation.getMerchant().getUser(),
                    NotificationType.RESERVATION_CANCELLED,
                    "Reservation cancelled by customer",
                    "The customer cancelled their reservation: " + request.reason(),
                    reservation.getId()
            );
        }
        Reservation updatedReservation = reservationRepository.save(reservation);
        return reservationMapper.toResponse(updatedReservation);
    }
}
