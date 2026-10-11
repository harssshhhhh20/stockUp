package com.stockup.backend.domain.pricing.service;

import com.stockup.backend.domain.basket.entity.BasketItem;
import com.stockup.backend.domain.pricing.entity.StoreItemPrice;
import com.stockup.backend.domain.pricing.repository.StoreItemPriceRepository;
import com.stockup.backend.domain.store.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fills in the prices so the shopkeeper doesn't have to.
 *
 * A shop replying to a list should never see an empty price column. Each line
 * arrives already carrying a number, and the shopkeeper's job is to correct the
 * odd one rather than enter twelve. Where the number comes from, in order:
 *
 *   1. What this shop last charged for it — its own price, its own margin.
 *   2. What the shopper said they expect to pay — a fair opening guess.
 *   3. Nothing, and the line stays blank rather than inventing a figure.
 *
 * Rule three matters more than it looks. A made-up price the shopkeeper doesn't
 * notice becomes a price the customer is quoted, and an argument at the counter.
 */
@Service
@RequiredArgsConstructor
public class SuggestedPricing {

    private final StoreItemPriceRepository priceRepository;

    /** Where a suggested price came from, so the app can say so honestly. */
    public enum Source { SHOP_MEMORY, SHOPPER_ESTIMATE, NONE }

    public record Suggestion(BigDecimal unitPrice, Source source) {
        public boolean isKnown() {
            return unitPrice != null;
        }
    }

    /**
     * One query per reply, then matched in memory — a shop's price book is a few
     * hundred rows at most, and the shopkeeper is waiting.
     */
    @Transactional(readOnly = true)
    public Map<java.util.UUID, Suggestion> suggestFor(Store store, List<BasketItem> items) {
        Map<String, StoreItemPrice> remembered = new HashMap<>();
        for (StoreItemPrice price : priceRepository.findAllByStore(store)) {
            remembered.put(price.getProductKey() + "|" + price.getUnit(), price);
        }

        Map<java.util.UUID, Suggestion> out = new HashMap<>();
        for (BasketItem item : items) {
            String key = StoreItemPrice.key(item.getProductName()) + "|" + item.getUnit();
            StoreItemPrice known = remembered.get(key);

            if (known != null) {
                out.put(item.getId(), new Suggestion(known.getUnitPrice(), Source.SHOP_MEMORY));
            } else if (item.getExpectedUnitPrice() != null) {
                out.put(item.getId(), new Suggestion(item.getExpectedUnitPrice(), Source.SHOPPER_ESTIMATE));
            } else {
                out.put(item.getId(), new Suggestion(null, Source.NONE));
            }
        }
        return out;
    }

    /**
     * Learns from what the shop actually quoted, so the next reply is faster.
     *
     * Only real quotes are remembered: a price the shopkeeper never looked at
     * shouldn't harden into his catalogue.
     */
    @Transactional
    public void remember(Store store, String productName, com.stockup.backend.domain.basket.enums.BasketItemUnit unit, BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.signum() <= 0) return;

        String key = StoreItemPrice.key(productName);
        if (key.isBlank()) return;

        priceRepository.findByStoreAndProductKeyAndUnit(store, key, unit)
                .ifPresentOrElse(
                        existing -> existing.remember(unitPrice),
                        () -> priceRepository.save(
                                StoreItemPrice.first(store, productName, unit, unitPrice))
                );
    }
}
