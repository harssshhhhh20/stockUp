package com.stockup.backend.domain.feedback.service;

import com.stockup.backend.domain.feedback.dto.FeedbackResponse;
import com.stockup.backend.domain.feedback.dto.StoreFeedbackSummary;
import com.stockup.backend.domain.feedback.dto.SubmitFeedbackRequest;

import java.util.UUID;

public interface FeedbackService {

    FeedbackResponse submit(UUID reservationId, SubmitFeedbackRequest request);

    FeedbackResponse forReservation(UUID reservationId);

    StoreFeedbackSummary forStore(UUID storeId, int limit);
}
