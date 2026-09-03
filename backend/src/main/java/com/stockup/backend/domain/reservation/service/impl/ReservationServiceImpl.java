package com.stockup.backend.domain.reservation.service.impl;

import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.basket.enums.BasketStatus;
import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.merchant.exception.MerchantNotFoundException;
import com.stockup.backend.domain.merchant.repository.MerchantRepository;
import com.stockup.backend.domain.merchant.service.BharosaScoreService;
import com.stockup.backend.domain.merchantoffer.entity.MerchantOffer;
import com.stockup.backend.domain.merchantoffer.enums.MerchantOfferStatus;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
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

        // A list is reserved at exactly one shop. Guarding only the offer let a
        // customer accept every reply they got, leaving three shopkeepers each
        // holding stock for one basket that only one of them would ever hand
        // over — and the two who lost took the Bharosa hit for it.
        if (basket.getStatus() != BasketStatus.ACTIVE) {
            throw new ReservationAlreadyExistsException(
                    "You've already reserved this list at a shop."
            );
        }
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

        // Hold the list and the chosen offer so neither can be promised twice.
        // These transitions existed on the entities but were never invoked,
        // which is why the button stayed live after a successful reservation.
        //
        // The other shops are deliberately left alone until activation: for the
        // next two minutes nothing is settled, and marking them as passed over
        // would be recording a decision the customer can still take back.
        merchantOffer.markReserved();
        basket.reserve();

        eventRecorder.recordReservationStage(
                reservation, ReservationEventType.CUSTOMER_RESERVED, EventActor.CUSTOMER, null);

        // The shop is told at activation, not here. Notifying now would have a
        // shopkeeper pulling stock off shelves during the very window the
        // customer is still free to walk away from — the grace period would
        // cost the one person it was never meant to cost.

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

        // The grace window has closed, so the other shops have genuinely been
        // passed over now — and only now is that safe to write down.
        merchantOfferRepository
                .findAllByBroadcastRecipient_Broadcast_Basket(reservation.getBasket())
                .stream()
                .filter(other -> !other.getId().equals(reservation.getMerchantOffer().getId()))
                .filter(other -> other.getStatus() == MerchantOfferStatus.SUBMITTED)
                .forEach(MerchantOffer::markNotSelected);

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

        releaseHold(reservation);

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

        boolean viewingAsCustomer = reservation.getCustomer().getId().equals(currUser.getId());
        if (viewingAsCustomer || merchant.isEmpty()) {
            reservation.validateCustomer(currUser);
        } else {
            reservation.validateMerchant(merchant.get());
        }

        // Only the person collecting the order gets to see the code for it.
        return reservationMapper.toResponse(reservation, viewingAsCustomer);

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

        boolean ownReservation = reservation.getCustomer().getId().equals(currUser.getId());

        if (merchant.isPresent() && !ownReservation) {
            reservation.validateMerchant(merchant.get());
            reservation.cancelByMerchant(request.reason());

            eventRecorder.recordReservationStage(
                    reservation, ReservationEventType.MERCHANT_CANCELLED, EventActor.MERCHANT, null);

            releaseHold(reservation);

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

            // Put the list back the way it was. A customer cancellation can only
            // happen before activation, so the shop was never told and the other
            // replies were never touched — undoing the hold leaves the shopper
            // free to choose again instead of stranded with a dead list.
            releaseHold(reservation);

            // Deliberately no merchant notification: they never heard about this
            // reservation, and "cancelled" is a confusing first thing to learn
            // about an order you never knew you had.
        }
        Reservation updatedReservation = reservationRepository.save(reservation);
        return reservationMapper.toResponse(updatedReservation);
    }

    /**
     * Undoes the hold a reservation placed on a list, so a reservation that
     * falls through doesn't take the shopping list down with it.
     *
     * Best-effort by design: this runs on cancellation and expiry paths where
     * the reservation outcome is what actually matters, and a list that has
     * since expired on its own is simply left alone.
     */
    private void releaseHold(Reservation reservation) {
        try {
            MerchantOffer offer = reservation.getMerchantOffer();
            if (offer.getStatus() == MerchantOfferStatus.RESERVED) {
                offer.unreserve();
            }

            Basket basket = reservation.getBasket();
            if (basket.getStatus() == BasketStatus.RESERVED) {
                basket.reopen();
            }
        } catch (RuntimeException ex) {
            log.warn(
                    "Could not reopen basket for reservation {}: {}",
                    reservation.getId(),
                    ex.getMessage()
            );
        }
    }
}
