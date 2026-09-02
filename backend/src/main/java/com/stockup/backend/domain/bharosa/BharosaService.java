package com.stockup.backend.domain.bharosa;

import com.stockup.backend.domain.bharosa.dto.BharosaResponse;
import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.store.entity.Store;

import java.util.UUID;

public interface BharosaService {

    BharosaResponse forStore(UUID storeId);

    BharosaResponse forMerchant(Merchant merchant, Store store);

    /** Recompute and persist. Called when something happens, and nightly. */
    int recompute(UUID merchantId);
}
