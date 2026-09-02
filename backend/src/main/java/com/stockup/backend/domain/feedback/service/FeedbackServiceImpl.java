package com.stockup.backend.domain.feedback.service;

import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.feedback.dto.FeedbackResponse;
import com.stockup.backend.domain.feedback.dto.StoreFeedbackSummary;
import com.stockup.backend.domain.feedback.dto.SubmitFeedbackRequest;
import com.stockup.backend.domain.feedback.entity.ReservationFeedback;
import com.stockup.backend.domain.feedback.exception.FeedbackAlreadyGivenException;
import com.stockup.backend.domain.feedback.exception.FeedbackNotAllowedException;
import com.stockup.backend.domain.feedback.repository.ReservationFeedbackRepository;
import com.stockup.backend.domain.merchant.service.BharosaScoreService;
import com.stockup.backend.domain.reservation.entity.Reservation;
import com.stockup.backend.domain.reservation.entity.enums.ReservationStatus;
import com.stockup.backend.domain.reservation.event.EventActor;
import com.stockup.backend.domain.reservation.event.ReservationEventRecorder;
import com.stockup.backend.domain.reservation.event.ReservationEventType;
import com.stockup.backend.domain.reservation.exception.ReservationNotFoundException;
import com.stockup.backend.domain.reservation.repository.ReservationRepository;
import com.stockup.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackServiceImpl implements FeedbackService {

    private final ReservationFeedbackRepository repository;
    private final ReservationRepository reservationRepository;
    private final CurrentUserService currentUserService;
    private final ReservationEventRecorder eventRecorder;
    private final BharosaScoreService bharosaScoreService;

    @Override
    public FeedbackResponse submit(UUID reservationId, SubmitFeedbackRequest request) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found."));

        User current = currentUserService.getCurrentUser();

        // Only the person who placed the order may rate it.
        if (!reservation.getCustomer().getId().equals(current.getId())) {
            throw new FeedbackNotAllowedException("You can only rate your own orders.");
        }

        // Only outcomes the merchant materially caused are rateable. Rating a
        // reservation you cancelled yourself would be rating your own decision.
        boolean rateable = reservation.getStatus() == ReservationStatus.COMPLETED
                || reservation.getStatus() == ReservationStatus.MERCHANT_CANCELLED
                || reservation.getStatus() == ReservationStatus.EXPIRED;
        if (!rateable) {
            throw new FeedbackNotAllowedException(
                    "This order can't be rated yet — rate it once it's completed.");
        }

        if (repository.existsByReservationId(reservationId)) {
            throw new FeedbackAlreadyGivenException("You've already rated this order.");
        }

        ReservationFeedback feedback = repository.save(ReservationFeedback.of(
                reservationId,
                current.getId(),
                reservation.getMerchant().getId(),
                reservation.getStore().getId(),
                request.stars().shortValue(),
                request.repliedFast(),
                request.readyOnTime(),
                request.stockAccurate(),
                request.comment()
        ));

        eventRecorder.recordReservationStage(
                reservation, ReservationEventType.FEEDBACK_SUBMITTED, EventActor.CUSTOMER, null);

        // New evidence — let the score take it into account straight away.
        bharosaScoreService.adjust(reservation.getMerchant(), 0, "customer feedback received");

        return toResponse(feedback, current);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackResponse forReservation(UUID reservationId) {
        return repository.findByReservationId(reservationId)
                .map(f -> toResponse(f, null))
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreFeedbackSummary forStore(UUID storeId, int limit) {
        var page = repository.findByStoreIdOrderByCreatedAtDesc(
                storeId, PageRequest.of(0, limit));

        List<ReservationFeedback> all = page.getContent();

        Double avg = all.isEmpty() ? null
                : all.stream().mapToInt(ReservationFeedback::getStars).average().orElse(0);

        return new StoreFeedbackSummary(
                avg,
                page.getTotalElements(),
                commonlySaid(all),
                all.stream().map(f -> toResponse(f, null)).toList()
        );
    }

    /**
     * "Customers commonly say…" — derived from chip agreement, never written by
     * hand. Only claims backed by a clear majority are surfaced, so one happy
     * customer doesn't put words in everyone's mouth.
     */
    private List<String> commonlySaid(List<ReservationFeedback> feedback) {
        List<String> out = new ArrayList<>();
        if (feedback.size() < 3) return out;

        addIfMostlyTrue(out, feedback, ReservationFeedback::getRepliedFast, "⚡ Replies quickly");
        addIfMostlyTrue(out, feedback, ReservationFeedback::getReadyOnTime, "📦 Usually ready on time");
        addIfMostlyTrue(out, feedback, ReservationFeedback::getStockAccurate, "✅ Availability is accurate");
        return out;
    }

    private void addIfMostlyTrue(List<String> out,
                                 List<ReservationFeedback> feedback,
                                 java.util.function.Function<ReservationFeedback, Boolean> chip,
                                 String label) {
        long answered = feedback.stream().filter(f -> chip.apply(f) != null).count();
        if (answered < 3) return;
        long yes = feedback.stream().filter(f -> Boolean.TRUE.equals(chip.apply(f))).count();
        if ((double) yes / answered >= 0.7) out.add(label);
    }

    private FeedbackResponse toResponse(ReservationFeedback f, User reviewer) {
        String name = reviewer != null && reviewer.getFirstName() != null
                ? reviewer.getFirstName()
                : "A shopper";

        return new FeedbackResponse(
                f.getId(),
                f.getReservationId(),
                f.getStars(),
                f.getRepliedFast(),
                f.getReadyOnTime(),
                f.getStockAccurate(),
                f.getComment(),
                true,
                name,
                f.getCreatedAt()
        );
    }
}
