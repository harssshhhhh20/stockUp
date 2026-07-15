package com.stockup.backend.domain.store.repository;

import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.store.entity.Store;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    boolean existsByMerchant(Merchant merchant);

    Optional<Store> findByMerchant(Merchant merchant);

    @Query(value = """
        SELECT *
        FROM stores
        WHERE (
            6371000 * acos(
                cos(radians(:latitude))
                * cos(radians(latitude))
                * cos(radians(longitude) - radians(:longitude))
                + sin(radians(:latitude))
                * sin(radians(latitude))
            )
        ) <= :radiusInMeters
        """, nativeQuery = true)
    List<Store> findNearbyStores(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusInMeters") Integer radiusInMeters
    );
}
