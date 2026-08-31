package com.stockup.backend.domain.merchantoffer.service;

import com.stockup.backend.domain.merchantoffer.dto.MerchantOfferSummaryResponse;
import com.stockup.backend.domain.merchantoffer.dto.SubmitMerchantOfferRequest;
import com.stockup.backend.domain.merchantoffer.dto.SubmitMerchantOfferResponse;

import java.util.List;
import java.util.UUID;

public interface MerchantOfferService {

    SubmitMerchantOfferResponse submit(SubmitMerchantOfferRequest request);

    List<MerchantOfferSummaryResponse> getOffersForBasket(UUID basketId);

}