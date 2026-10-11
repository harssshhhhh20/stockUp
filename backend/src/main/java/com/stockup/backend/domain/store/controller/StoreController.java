package com.stockup.backend.domain.store.controller;

import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.domain.store.dto.request.CreateStoreRequest;
import com.stockup.backend.domain.store.dto.response.MenuImageContent;
import com.stockup.backend.domain.store.dto.response.MenuImageResponse;
import com.stockup.backend.domain.store.dto.response.StoreResponse;
import com.stockup.backend.domain.store.service.StoreMenuService;
import com.stockup.backend.domain.store.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService storeService;
    private final StoreMenuService storeMenuService;

    public StoreController(StoreService storeService, StoreMenuService storeMenuService) {
        this.storeService = storeService;
        this.storeMenuService = storeMenuService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> createStore(
            @Valid @RequestBody CreateStoreRequest request
    ) {

        storeService.createStore(request);

        return ApiResponseFactory.success(
                ResponseMessage.STORE_CREATED,
                null
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StoreResponse>> getMyStore() {

        return ApiResponseFactory.success(
                ResponseMessage.FETCHED,
                storeService.getMyStore().orElse(null)
        );
    }

    /** One page per request, so a flaky connection only ever loses one photo. */
    @PostMapping(value = "/me/menu-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MenuImageResponse>> uploadMenuImage(
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.CREATED,
                storeMenuService.uploadToMyStore(file)
        );
    }

    @DeleteMapping("/me/menu-images/{imageId}")
    public ResponseEntity<ApiResponse<Object>> deleteMenuImage(@PathVariable UUID imageId) {

        storeMenuService.deleteFromMyStore(imageId);

        return ApiResponseFactory.success(ResponseMessage.DELETED, null);
    }

    @GetMapping("/{storeId}/menu-images")
    public ResponseEntity<ApiResponse<List<MenuImageResponse>>> listMenuImages(
            @PathVariable UUID storeId
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.FETCHED,
                storeMenuService.list(storeId)
        );
    }

    /**
     * The image bytes themselves. Public (see SecurityConfig) so an {@code <Image>}
     * can load it directly — a menu is what a restaurant shows anyone who walks
     * in, and the ids are unguessable. A page is never edited in place (only
     * added or deleted), so it can be cached for good.
     */
    @GetMapping("/{storeId}/menu-images/{imageId}")
    public ResponseEntity<byte[]> menuImage(
            @PathVariable UUID storeId,
            @PathVariable UUID imageId
    ) {
        MenuImageContent content = storeMenuService.content(storeId, imageId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .header("X-Content-Type-Options", "nosniff")
                .body(content.data());
    }
}
