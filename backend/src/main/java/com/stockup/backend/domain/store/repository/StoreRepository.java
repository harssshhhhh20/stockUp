package com.stockup.backend.domain.store.repository;

import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    boolean existsByMerchant(Merchant merchant);

    Optional<Store> findByMerchant(Merchant merchant);
}
