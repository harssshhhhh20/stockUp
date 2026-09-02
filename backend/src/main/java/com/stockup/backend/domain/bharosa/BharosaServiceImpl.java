package com.stockup.backend.domain.bharosa;

import com.stockup.backend.domain.bharosa.dto.BharosaResponse;
import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.merchant.repository.MerchantRepository;
import com.stockup.backend.domain.store.entity.Store;
import com.stockup.backend.domain.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BharosaServiceImpl implements BharosaService {

    private final BharosaEngine engine;
    private final BharosaExplainer explainer;
    private final StoreRepository storeRepository;
    private final MerchantRepository merchantRepository;

    @Override
    public BharosaResponse forStore(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found."));
        return forMerchant(store.getMerchant(), store);
    }

    @Override
    public BharosaResponse forMerchant(Merchant merchant, Store store) {
        BharosaPillars pillars = engine.computePillars(merchant.getId());
        int score = engine.score(pillars);

        return new BharosaResponse(
                merchant.getId(),
                store != null ? store.getId() : null,
                store != null ? store.getName() : null,
                score,
                band(score, pillars),
                pillars.isUnproven(),
                explainer.bannerTags(pillars),
                explainer.reasons(pillars),
                pillars.totalEvents(),
                pillars.distinctCustomers()
        );
    }

    /**
     * Writes the freshly computed score back onto the merchant, so ranking and
     * listing queries can sort without recomputing per row.
     */
    @Override
    @Transactional
    public int recompute(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new EntityNotFoundException("Merchant not found."));

        int score = engine.scoreFor(merchantId);
        merchant.setBharosaScore(score);
        merchantRepository.save(merchant);

        return score;
    }

    /**
     * Bands drive colour, and map onto the app's existing semantics: an unproven
     * shop is *info*, never *attention* — being new is a fact, not a warning.
     */
    private String band(int score, BharosaPillars pillars) {
        if (pillars.isUnproven()) return "new";
        if (score >= 75) return "trusted";
        if (score >= 45) return "mixed";
        return "risky";
    }
}
