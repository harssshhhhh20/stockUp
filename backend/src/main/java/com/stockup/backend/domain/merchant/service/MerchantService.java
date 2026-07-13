package com.stockup.backend.domain.merchant.service;

import com.stockup.backend.domain.merchant.dto.request.CreateMerchantRequest;

public interface MerchantService {

    void registerMerchant(CreateMerchantRequest request);

}
