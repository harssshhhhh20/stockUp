package com.stockup.backend.domain.store.service;

import com.stockup.backend.domain.store.dto.response.MenuImageContent;
import com.stockup.backend.domain.store.dto.response.MenuImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface StoreMenuService {

    MenuImageResponse uploadToMyStore(MultipartFile file);

    void deleteFromMyStore(UUID imageId);

    List<MenuImageResponse> list(UUID storeId);

    MenuImageContent content(UUID storeId, UUID imageId);
}
