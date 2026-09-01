package com.stockup.backend.domain.store.service;

import com.stockup.backend.domain.store.dto.request.CreateStoreRequest;
import com.stockup.backend.domain.store.dto.response.StoreResponse;

import java.util.Optional;

public interface StoreService {

    void createStore(CreateStoreRequest request);

    Optional<StoreResponse> getMyStore();
}
