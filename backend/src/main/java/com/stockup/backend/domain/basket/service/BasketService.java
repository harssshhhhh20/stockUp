package com.stockup.backend.domain.basket.service;

import com.stockup.backend.domain.basket.dto.request.CreateBasketRequest;
import com.stockup.backend.domain.basket.dto.response.BasketDetailsResponse;
import com.stockup.backend.domain.basket.dto.response.BasketHistoryResponse;
import com.stockup.backend.domain.basket.dto.response.CreateBasketResponse;

import java.util.List;
import java.util.UUID;

public interface BasketService {

    CreateBasketResponse createBasket(CreateBasketRequest request);

    List<BasketHistoryResponse> getBasketHistory();

    BasketDetailsResponse getBasket(UUID basketId);

}