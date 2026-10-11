package com.stockup.backend.domain.store.repository;

import com.stockup.backend.domain.store.entity.StoreMenuImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreMenuImageRepository extends JpaRepository<StoreMenuImage, UUID> {

    /** Everything about each page except the bytes, which can be megabytes. */
    interface Summary {
        UUID getId();

        int getSortOrder();
    }

    @Query("""
            SELECT i.id AS id, i.sortOrder AS sortOrder
            FROM StoreMenuImage i
            WHERE i.store.id = :storeId
            ORDER BY i.sortOrder, i.createdAt
            """)
    List<Summary> findSummaries(@Param("storeId") UUID storeId);

    long countByStore_Id(UUID storeId);

    @Query("SELECT COALESCE(MAX(i.sortOrder), 0) FROM StoreMenuImage i WHERE i.store.id = :storeId")
    int maxSortOrder(@Param("storeId") UUID storeId);

    Optional<StoreMenuImage> findByIdAndStore_Id(UUID id, UUID storeId);
}
