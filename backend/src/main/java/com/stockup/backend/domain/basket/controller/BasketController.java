package com.stockup.backend.domain.basket.controller;

import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.domain.basket.dto.request.CreateBasketRequest;
import com.stockup.backend.domain.basket.dto.response.BasketDetailsResponse;
import com.stockup.backend.domain.basket.dto.response.BasketHistoryResponse;
import com.stockup.backend.domain.basket.dto.response.CreateBasketResponse;
import com.stockup.backend.domain.basket.service.BasketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/baskets")
@RequiredArgsConstructor
public class BasketController {

    private final BasketService basketService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateBasketResponse>> createBasket(
            @Valid @RequestBody CreateBasketRequest request
    ) {
        CreateBasketResponse response = basketService.createBasket(request);

        return ApiResponseFactory.success(
                ResponseMessage.BASKET_CREATED,
                response
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BasketHistoryResponse>>> getBasketHistory() {

        return ApiResponseFactory.success(
                ResponseMessage.BASKET_HISTORY_FETCHED,
                basketService.getBasketHistory()
        );
    }

    @GetMapping("/{basketId}")
    public ResponseEntity<ApiResponse<BasketDetailsResponse>> getBasket(
            @PathVariable UUID basketId
    ) {

        return ApiResponseFactory.success(
                ResponseMessage.BASKET_FETCHED,
                basketService.getBasket(basketId)
        );
    }
}