package com.stockup.backend.domain.bharosa.controller;

import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.domain.bharosa.BharosaService;
import com.stockup.backend.domain.bharosa.dto.BharosaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Bharosa is public, aggregate reputation — any signed-in user may read any
 * shop's score. Nothing here exposes who transacted with whom.
 */
@RestController
@RequestMapping("/api/v1/bharosa")
@RequiredArgsConstructor
public class BharosaController {

    private final BharosaService bharosaService;

    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<BharosaResponse>> forStore(@PathVariable UUID storeId) {
        return ApiResponseFactory.success(
                ResponseMessage.FETCHED,
                bharosaService.forStore(storeId)
        );
    }
}
