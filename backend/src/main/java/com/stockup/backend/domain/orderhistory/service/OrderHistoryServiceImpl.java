package com.stockup.backend.domain.orderhistory.service;

import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.bharosa.BharosaEngine;
import com.stockup.backend.domain.feedback.repository.ReservationFeedbackRepository;
import com.stockup.backend.domain.feedback.service.FeedbackService;
import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.merchant.repository.MerchantRepository;
import com.stockup.backend.domain.orderhistory.dto.MerchantStatsResponse;
import com.stockup.backend.domain.orderhistory.dto.OrderDetailResponse;
import com.stockup.backend.domain.orderhistory.dto.OrderTimelineEntry;
import com.stockup.backend.domain.reservation.entity.Reservation;
import com.stockup.backend.domain.reservation.entity.enums.ReservationStatus;
import com.stockup.backend.domain.reservation.event.ReservationEvent;
import com.stockup.backend.domain.reservation.event.ReservationEventRepository;
import com.stockup.backend.domain.reservation.event.ReservationEventType;
import com.stockup.backend.domain.reservation.exception.ReservationAccessDeniedException;
import com.stockup.backend.domain.reservation.exception.ReservationNotFoundException;
import com.stockup.backend.domain.reservation.repository.ReservationRepository;
import com.stockup.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderHistoryServiceImpl implements OrderHistoryService {

    private final ReservationRepository reservationRepository;
    private final ReservationEventRepository eventRepository;
    private final ReservationFeedbackRepository feedbackRepository;
    private final FeedbackService feedbackService;
    private final MerchantRepository merchantRepository;
    private final CurrentUserService currentUserService;
    private final BharosaEngine bharosaEngine;

    /**
     * Order detail is private to the two parties. Enforced here rather than in
     * the client, so no other shopper and no other shop can read it.
     */
    @Override
    public OrderDetailResponse getOrder(UUID reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Order not found."));

        User current = currentUserService.getCurrentUser();
        Optional<Merchant> asMerchant = merchantRepository.findByUser(current);

        boolean isCustomer = reservation.getCustomer().getId().equals(current.getId());
        boolean isTheirMerchant = asMerchant
                .map(m -> m.getId().equals(reservation.getMerchant().getId()))
                .orElse(false);

        if (!isCustomer && !isTheirMerchant) {
            throw new ReservationAccessDeniedException("This isn't your order.");
        }

        List<ReservationEvent> events =
                eventRepository.findByReservationIdOrderByOccurredAtAsc(reservationId);

        // The pre-reservation part of the story lives against the basket.
        List<ReservationEvent> basketEvents =
                eventRepository.findByBasketIdOrderByOccurredAtAsc(reservation.getBasket().getId())
                        .stream()
                        .filter(e -> e.getMerchantId() == null
                                || e.getMerchantId().equals(reservation.getMerchant().getId()))
                        .toList();

        List<ReservationEvent> merged = new ArrayList<>(basketEvents);
        for (ReservationEvent e : events) {
            if (merged.stream().noneMatch(m -> m.getId().equals(e.getId()))) merged.add(e);
        }
        merged.sort(Comparator.comparing(ReservationEvent::getOccurredAt));

        var feedback = feedbackRepository.existsByReservationId(reservationId)
                ? feedbackService.forReservation(reservationId)
                : null;

        boolean rateable = feedback == null && switch (reservation.getStatus()) {
            case COMPLETED, MERCHANT_CANCELLED, EXPIRED -> true;
            default -> false;
        };

        return new OrderDetailResponse(
                reservation.getId(),
                reservation.getBasket().getId(),
                reservation.getStore().getId(),
                reservation.getStore().getName(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getActiveAt(),
                reservation.getBasket().getItems().stream()
                        .map(i -> i.getQuantity().stripTrailingZeros().toPlainString()
                                + " " + i.getUnit().name().toLowerCase() + " · " + i.getProductName())
                        .toList(),
                secondsBetween(merged, ReservationEventType.REQUEST_BROADCAST,
                        ReservationEventType.OFFER_SUBMITTED),
                secondsBetween(merged, ReservationEventType.CUSTOMER_RESERVED,
                        ReservationEventType.HANDOVER_COMPLETED),
                reservation.getCancellationReason(),
                merged.stream().map(this::toTimelineEntry).toList(),
                feedback,
                rateable && isCustomer
        );
    }

    @Override
    public MerchantStatsResponse merchantStats(int windowDays) {

        User current = currentUserService.getCurrentUser();
        Merchant merchant = merchantRepository.findByUser(current)
                .orElseThrow(() -> new ReservationAccessDeniedException("You don't have a shop."));

        Instant since = Instant.now().minus(Duration.ofDays(windowDays));
        UUID id = merchant.getId();

        long broadcast = eventRepository.countByMerchantIdAndEventTypeAndOccurredAtAfter(
                id, ReservationEventType.REQUEST_BROADCAST, since);
        long answered = eventRepository.countByMerchantIdAndEventTypeAndOccurredAtAfter(
                id, ReservationEventType.OFFER_SUBMITTED, since);
        long completed = eventRepository.countByMerchantIdAndEventTypeAndOccurredAtAfter(
                id, ReservationEventType.HANDOVER_COMPLETED, since);
        long cancelled = eventRepository.countByMerchantIdAndEventTypeAndOccurredAtAfter(
                id, ReservationEventType.MERCHANT_CANCELLED, since);
        long expired = eventRepository.countByMerchantIdAndEventTypeAndOccurredAtAfter(
                id, ReservationEventType.RESERVATION_EXPIRED, since);
        long reserved = eventRepository.countByMerchantIdAndEventTypeAndOccurredAtAfter(
                id, ReservationEventType.CUSTOMER_RESERVED, since);

        Double median = eventRepository.medianResponseSeconds(id, since);
        long distinct = eventRepository.distinctCompletedCustomers(id, since);

        long settled = completed + cancelled + expired;

        return new MerchantStatsResponse(
                completed,
                median == null ? null : Math.round(median),
                broadcast == 0 ? null : (double) answered / broadcast,
                settled == 0 ? null : (double) completed / settled,
                settled == 0 ? null : (double) cancelled / settled,
                distinct == 0 || completed == 0 ? null
                        : 1.0 - ((double) distinct / completed),
                distinct,
                // Computed live from the same event log the rest of this
                // response reads. The stored column can lag a recompute, and a
                // merchant seeing two different Bharosa numbers would rightly
                // stop believing either.
                bharosaEngine.scoreFor(id)
        );
    }

    private Long secondsBetween(List<ReservationEvent> events,
                                ReservationEventType from, ReservationEventType to) {
        Instant a = firstOf(events, from);
        Instant b = firstOf(events, to);
        if (a == null || b == null || b.isBefore(a)) return null;
        return Duration.between(a, b).toSeconds();
    }

    private Instant firstOf(List<ReservationEvent> events, ReservationEventType type) {
        return events.stream()
                .filter(e -> e.getEventType() == type)
                .map(ReservationEvent::getOccurredAt)
                .findFirst().orElse(null);
    }

    /** Phrased from the shopper's side of the screen, not the schema's. */
    private OrderTimelineEntry toTimelineEntry(ReservationEvent e) {
        String label = switch (e.getEventType()) {
            case REQUEST_BROADCAST -> "Request sent to nearby shops";
            case MERCHANT_VIEWED -> "Shop opened your request";
            case OFFER_SUBMITTED -> "Shop replied with what they have";
            case REQUEST_VIEWED_THEN_EXPIRED -> "Shop didn't reply in time";
            case REQUEST_EXPIRED_UNSEEN -> "Request expired unopened";
            case CUSTOMER_RESERVED -> "You reserved";
            case RESERVATION_ACTIVATED -> "Reservation confirmed — collection code issued";
            case MERCHANT_READY -> "Ready for collection";
            case HANDOVER_COMPLETED -> "Collected";
            case MERCHANT_CANCELLED -> "Shop cancelled";
            case CUSTOMER_CANCELLED -> "You cancelled";
            case RESERVATION_EXPIRED -> "Reservation expired";
            case FEEDBACK_SUBMITTED -> "You rated this order";
        };
        return new OrderTimelineEntry(
                e.getEventType().name(), label, e.getActor().name(), e.getOccurredAt());
    }
}
