package com.stockup.backend.domain.store.service;

import com.stockup.backend.domain.store.dto.request.CreateStoreRequest;

public interface StoreService {

    void createStore(CreateStoreRequest request);
}
