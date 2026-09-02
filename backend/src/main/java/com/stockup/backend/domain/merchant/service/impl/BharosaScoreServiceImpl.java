package com.stockup.backend.domain.merchant.service.impl;

import com.stockup.backend.domain.bharosa.BharosaEngine;
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
    private final BharosaEngine bharosaEngine;

    /**
     * Kept as the call site the domain already uses, but the delta is now only a
     * hint that something changed. The score itself is recomputed from the event
     * log, so it can never drift away from what actually happened — and a bug in
     * one call site cannot permanently corrupt a merchant's reputation.
     */
    @Override
    public void adjust(Merchant merchant, int delta, String reason) {

        int recomputed = bharosaEngine.scoreFor(merchant.getId());

        merchant.setBharosaScore(recomputed);
        merchantRepository.save(merchant);

        log.info(
                "Recomputed Bharosa for merchant {} after '{}' -> {}",
                merchant.getId(), reason, recomputed
        );
    }
}
