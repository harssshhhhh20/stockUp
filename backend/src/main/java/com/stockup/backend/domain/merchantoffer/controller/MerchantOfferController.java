package com.stockup.backend.domain.merchantoffer.controller;

import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.domain.merchantoffer.dto.MerchantOfferSummaryResponse;
import com.stockup.backend.domain.merchantoffer.dto.SubmitMerchantOfferRequest;
import com.stockup.backend.domain.merchantoffer.dto.SubmitMerchantOfferResponse;
import com.stockup.backend.domain.merchantoffer.service.MerchantOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchant-offers")
@RequiredArgsConstructor
public class MerchantOfferController {

    private final MerchantOfferService merchantOfferService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubmitMerchantOfferResponse>> submit(
            @Valid @RequestBody SubmitMerchantOfferRequest request
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.MERCHANT_OFFER_SUBMITTED_SUCCESSFULLY,
                merchantOfferService.submit(request)
        );
    }

    @GetMapping("/basket/{basketId}")
    public ResponseEntity<ApiResponse<List<MerchantOfferSummaryResponse>>> getOffersForBasket(
            @PathVariable UUID basketId
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.FETCHED,
                merchantOfferService.getOffersForBasket(basketId)
        );
    }
}