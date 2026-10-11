package com.stockup.backend.domain.pricing.repository;

import com.stockup.backend.domain.pricing.entity.StoreItemPrice;
import com.stockup.backend.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreItemPriceRepository extends JpaRepository<StoreItemPrice, UUID> {

    Optional<StoreItemPrice> findByStoreAndProductKeyAndUnit(
            Store store, String productKey, com.stockup.backend.domain.basket.enums.BasketItemUnit unit);

    /** Every price this shop remembers — one query, then matched in memory. */
    List<StoreItemPrice> findAllByStore(Store store);
}
