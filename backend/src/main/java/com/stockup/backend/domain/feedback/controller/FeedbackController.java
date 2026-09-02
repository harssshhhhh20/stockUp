package com.stockup.backend.domain.feedback.controller;

import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.domain.feedback.dto.FeedbackResponse;
import com.stockup.backend.domain.feedback.dto.StoreFeedbackSummary;
import com.stockup.backend.domain.feedback.dto.SubmitFeedbackRequest;
import com.stockup.backend.domain.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /** Rate a completed order. Ownership and eligibility are enforced server-side. */
    @PostMapping("/reservation/{reservationId}")
    public ResponseEntity<ApiResponse<FeedbackResponse>> submit(
            @PathVariable UUID reservationId,
            @Valid @RequestBody SubmitFeedbackRequest request
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.CREATED,
                feedbackService.submit(reservationId, request)
        );
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<ApiResponse<FeedbackResponse>> forReservation(
            @PathVariable UUID reservationId
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.FETCHED,
                feedbackService.forReservation(reservationId)
        );
    }

    /** Public, aggregate reputation for a store page. */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<StoreFeedbackSummary>> forStore(
            @PathVariable UUID storeId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.FETCHED,
                feedbackService.forStore(storeId, Math.min(limit, 50))
        );
    }
}
