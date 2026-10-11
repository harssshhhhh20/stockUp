package com.stockup.backend.domain.store.entity;

import com.stockup.backend.common.persistence.entity.AuditableEntity;
import jakarta.persistence.*;

/**
 * One page of a restaurant's menu card, as a photo.
 *
 * The bytes live on this row, so never load these in bulk — listing goes
 * through {@code StoreMenuImageRepository.findSummaries}, which leaves the
 * image data behind.
 */
@Entity
@Table(name = "store_menu_images")
public class StoreMenuImage extends AuditableEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 50)
    private String contentType;

    @Column(nullable = false)
    private int sizeBytes;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] data;

    protected StoreMenuImage() {
    }

    public StoreMenuImage(Store store, String contentType, byte[] data, int sortOrder) {
        this.store = store;
        this.contentType = contentType;
        this.data = data;
        this.sizeBytes = data.length;
        this.sortOrder = sortOrder;
    }

    public Store getStore() {
        return store;
    }

    public String getContentType() {
        return contentType;
    }

    public int getSizeBytes() {
        return sizeBytes;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public byte[] getData() {
        return data;
    }
}
