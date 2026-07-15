package com.stockup.backend.domain.store.service.impl;

import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.merchant.exception.MerchantNotFoundException;
import com.stockup.backend.domain.merchant.repository.MerchantRepository;
import com.stockup.backend.domain.store.dto.request.CreateStoreRequest;
import com.stockup.backend.domain.store.entity.Store;
import com.stockup.backend.domain.store.exception.StoreAlreadyExistsException;
import com.stockup.backend.domain.store.repository.StoreRepository;
import com.stockup.backend.domain.store.service.StoreService;
import com.stockup.backend.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final MerchantRepository merchantRepository;
    private final CurrentUserService currentUserService;

    public StoreServiceImpl(
            StoreRepository storeRepository,
            MerchantRepository merchantRepository,
            CurrentUserService currentUserService
    ) {
        this.storeRepository = storeRepository;
        this.merchantRepository = merchantRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    public void createStore(CreateStoreRequest request) {

        User user = currentUserService.getCurrentUser();

        Merchant merchant = merchantRepository.findByUser(user)
                .orElseThrow(MerchantNotFoundException::new);

        if (storeRepository.existsByMerchant(merchant)) {
            throw new StoreAlreadyExistsException();
        }

        Store store = new Store(
                merchant,
                request.name(),
                request.businessType(),
                request.addressLine1(),
                request.addressLine2(),
                request.city(),
                request.state(),
                request.postalCode(),
                request.country()
        );

        store.updateCoordinates(
                request.latitude(),
                request.longitude()
        );

        storeRepository.save(store);
    }
}