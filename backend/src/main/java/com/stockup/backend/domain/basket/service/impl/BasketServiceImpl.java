package com.stockup.backend.domain.basket.service.impl;

import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.basket.dto.request.CreateBasketItemRequest;
import com.stockup.backend.domain.basket.dto.request.CreateBasketRequest;
import com.stockup.backend.domain.basket.dto.response.BasketDetailsResponse;
import com.stockup.backend.domain.basket.dto.response.BasketHistoryResponse;
import com.stockup.backend.domain.basket.dto.response.BasketItemResponse;
import com.stockup.backend.domain.basket.dto.response.CreateBasketResponse;
import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.basket.enums.BasketTargetMode;
import com.stockup.backend.domain.basket.enums.BasketStatus;
import com.stockup.backend.domain.basket.exception.ActiveBasketAlreadyExistsException;
import com.stockup.backend.domain.basket.exception.InvalidBasketRequestException;
import com.stockup.backend.domain.basket.repository.BasketRepository;
import com.stockup.backend.domain.basket.service.BasketService;
import com.stockup.backend.domain.store.entity.Store;
import com.stockup.backend.domain.store.repository.StoreRepository;
import com.stockup.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BasketServiceImpl implements BasketService {

    private final BasketRepository basketRepository;
    private final StoreRepository storeRepository;
    private final CurrentUserService currentUserService;

    @Override
    public CreateBasketResponse createBasket(CreateBasketRequest request) {

        User customer = currentUserService.getCurrentUser();

        if (basketRepository.existsByCustomerAndStatus(customer, BasketStatus.ACTIVE)) {
            throw new ActiveBasketAlreadyExistsException("You already have an active basket.");
        }

        validateCreateBasketRequest(request);

        Basket basket = Basket.create(
                customer,
                request.targetMode(),
                request.searchRadiusMeters(),
                request.basketLatitude(),
                request.basketLongitude()
        );

        for (CreateBasketItemRequest item : request.items()) {
            basket.addItem(
                    item.productName(),
                    item.quantity(),
                    item.unit(),
                    item.brand(),
                    item.notes()
            );
        }

        if (request.targetMode() == BasketTargetMode.SELECTED_STORES) {

            List<Store> stores = storeRepository.findAllById(request.storeIds());

            if (stores.size() != request.storeIds().size()) {
                throw new InvalidBasketRequestException(
                        "One or more selected stores do not exist."
                );
            }

            stores.forEach(basket::addTargetStore);
        }

        Basket savedBasket = basketRepository.save(basket);

        return new CreateBasketResponse(savedBasket.getId());
    }

    private void validateCreateBasketRequest(CreateBasketRequest request) {
        switch (request.targetMode()) {
            case NEARBY -> {
                if (request.searchRadiusMeters() == null || request.searchRadiusMeters() <= 0) {
                    throw new InvalidBasketRequestException(
                            "Search radius must be greater than zero."
                    );
                }
            }
            case SELECTED_STORES -> {
                if (request.storeIds() == null || request.storeIds().isEmpty()) {
                    throw new InvalidBasketRequestException(
                            "At least one target store must be selected."
                    );
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BasketHistoryResponse> getBasketHistory() {
        User customer = currentUserService.getCurrentUser();
        return basketRepository
                .findAllByCustomerOrderByCreatedAtDesc(customer)
                .stream()
                .map(basket -> new BasketHistoryResponse(
                        basket.getId(),
                        basket.getStatus(),
                        basket.getCreatedAt(),
                        basket.getExpiresAt(),
                        basket.getItems().size(),
                        basket.getTargetMode()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BasketDetailsResponse getBasket(UUID basketId) {

        User customer = currentUserService.getCurrentUser();

        Basket basket = basketRepository
                .findByIdAndCustomer(basketId, customer)
                .orElseThrow(() ->
                        new InvalidBasketRequestException("Basket not found.")
                );

        return new BasketDetailsResponse(
                basket.getId(),
                basket.getStatus(),
                basket.getTargetMode(),
                basket.getSearchRadiusMeters(),
                basket.getBasketLatitude(),
                basket.getBasketLongitude(),
                basket.getCreatedAt(),
                basket.getExpiresAt(),
                basket.getItems()
                        .stream()
                        .map(item -> new BasketItemResponse(
                                item.getProductName(),
                                item.getQuantity(),
                                item.getUnit(),
                                item.getBrand(),
                                item.getNotes()
                        ))
                        .toList()
        );
    }
}