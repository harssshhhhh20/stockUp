package com.stockup.backend.domain.merchant.service.impl;

import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.merchant.repository.MerchantRepository;
import com.stockup.backend.domain.merchant.service.BharosaScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BharosaScoreServiceImpl implements BharosaScoreService {

    private final MerchantRepository merchantRepository;

    @Override
    public void adjust(Merchant merchant, int delta, String reason) {

        merchant.adjustBharosaScore(delta);

        merchantRepository.save(merchant);

        log.info(
                "Adjusted Bharosa Score for merchant {} by {} ({}). New score: {}",
                merchant.getId(),
                delta,
                reason,
                merchant.getBharosaScore()
        );
    }
}
