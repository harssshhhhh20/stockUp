package com.stockup.backend.domain.merchant.service;

import com.stockup.backend.domain.merchant.dto.request.CreateMerchantRequest;
import com.stockup.backend.domain.merchant.dto.response.MerchantProfileResponse;

import java.util.Optional;

public interface MerchantService {

    void registerMerchant(CreateMerchantRequest request);

    Optional<MerchantProfileResponse> getMyProfile();

}
