package com.stockup.backend.domain.merchant.controller;

import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.domain.merchant.dto.request.CreateMerchantRequest;
import com.stockup.backend.domain.merchant.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerMerchant(
            @RequestBody CreateMerchantRequest request
    ) {
        merchantService.registerMerchant(request);

        return ApiResponseFactory.success(
                ResponseMessage.MERCHANT_REGISTERED_SUCCESSFULLY,
                null
        );
    }
}