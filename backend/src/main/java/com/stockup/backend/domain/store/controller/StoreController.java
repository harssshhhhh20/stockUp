package com.stockup.backend.domain.store.controller;

import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.domain.store.dto.request.CreateStoreRequest;
import com.stockup.backend.domain.store.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
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
}