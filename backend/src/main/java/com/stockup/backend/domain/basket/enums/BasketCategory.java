package com.stockup.backend.domain.basket.enums;

import com.stockup.backend.domain.store.entity.enums.BusinessType;

/**
 * What kind of thing a shopper is asking for, which decides who gets asked.
 *
 * A kirana can't cook a thali and a restaurant doesn't stock toor dal, so a
 * request only ever reaches the shops that can answer it — anything else is a
 * notification the shopkeeper learns to ignore.
 */
public enum BasketCategory {

    GROCERY,

    FOOD;

    public boolean accepts(BusinessType businessType) {
        return switch (this) {
            case FOOD -> businessType == BusinessType.RESTAURANT;
            case GROCERY -> businessType != BusinessType.RESTAURANT;
        };
    }
}
