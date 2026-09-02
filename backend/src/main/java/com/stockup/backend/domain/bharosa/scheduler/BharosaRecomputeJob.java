package com.stockup.backend.domain.bharosa.scheduler;

import com.stockup.backend.domain.bharosa.BharosaService;
import com.stockup.backend.domain.merchant.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scores decay with time, so a shop that stops trading must drift back toward
 * the prior rather than freezing at yesterday's number. Event-driven recompute
 * alone would never fire for an inactive shop — hence the sweep.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BharosaRecomputeJob {

    private final MerchantRepository merchantRepository;
    private final BharosaService bharosaService;

    /** Nightly at 03:15, when nobody is shopping. */
    @Scheduled(cron = "0 15 3 * * *")
    public void recomputeAll() {
        var merchants = merchantRepository.findAll();
        int updated = 0;

        for (var merchant : merchants) {
            try {
                bharosaService.recompute(merchant.getId());
                updated++;
            } catch (Exception ex) {
                log.warn("Bharosa recompute failed for merchant {}: {}",
                        merchant.getId(), ex.getMessage());
            }
        }

        log.info("Bharosa nightly sweep complete: {}/{} merchants rescored",
                updated, merchants.size());
    }
}
