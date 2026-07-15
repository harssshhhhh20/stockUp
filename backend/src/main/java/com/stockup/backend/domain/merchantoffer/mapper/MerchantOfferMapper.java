package com.stockup.backend.domain.merchantoffer.mapper;

import com.stockup.backend.domain.basket.entity.BasketItem;
import com.stockup.backend.domain.merchantoffer.dto.SubmitMerchantOfferRequest;
import com.stockup.backend.domain.merchantoffer.value.MerchantOfferResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class MerchantOfferMapper {

    public List<MerchantOfferResponse> toResponses(
            SubmitMerchantOfferRequest request,
            Map<UUID, BasketItem> basketItems
    ) {

        return request.responses()
                .stream()
                .map(item -> {

                    BasketItem basketItem = basketItems.get(item.basketItemId());

                    if (basketItem == null) {
                        throw new IllegalArgumentException(
                                "Basket item not found: " + item.basketItemId()
                        );
                    }

                    return new MerchantOfferResponse(
                            basketItem,
                            item.status(),
                            item.availableQuantity()
                    );

                })
                .toList();
    }
}