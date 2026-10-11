package com.stockup.backend.domain.store.service.impl;

import com.stockup.backend.common.exceptions.model.BadRequestException;
import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.merchant.exception.MerchantNotFoundException;
import com.stockup.backend.domain.merchant.repository.MerchantRepository;
import com.stockup.backend.domain.store.dto.request.CreateStoreRequest;
import com.stockup.backend.domain.store.dto.response.StoreResponse;
import com.stockup.backend.domain.store.entity.Store;
import com.stockup.backend.domain.store.exception.StoreAlreadyExistsException;
import com.stockup.backend.domain.store.repository.StoreRepository;
import com.stockup.backend.domain.store.service.StoreService;
import com.stockup.backend.domain.user.entity.User;
import com.stockup.backend.infrastructure.geocoding.AddressGeocoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final MerchantRepository merchantRepository;
    private final CurrentUserService currentUserService;
    private final AddressGeocoder geocoder;

    public StoreServiceImpl(
            StoreRepository storeRepository,
            MerchantRepository merchantRepository,
            CurrentUserService currentUserService,
            AddressGeocoder geocoder
    ) {
        this.storeRepository = storeRepository;
        this.merchantRepository = merchantRepository;
        this.currentUserService = currentUserService;
        this.geocoder = geocoder;
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

        AddressGeocoder.Coordinates pin = resolveCoordinates(request);
        store.updateCoordinates(pin.latitude(), pin.longitude());

        storeRepository.save(store);
    }

    /**
     * Where the phone actually is beats any address lookup, so a shared
     * location always wins. Without one, the typed address is geocoded — and
     * if that fails too, the shopkeeper is asked to share location rather than
     * the shop being saved somewhere made up.
     */
    private AddressGeocoder.Coordinates resolveCoordinates(CreateStoreRequest request) {

        if (request.latitude() != null && request.longitude() != null) {
            return new AddressGeocoder.Coordinates(request.latitude(), request.longitude());
        }
        if (request.latitude() != null || request.longitude() != null) {
            throw new BadRequestException("Send both latitude and longitude, or neither.");
        }

        return geocoder.geocode(
                        request.addressLine1(),
                        request.city(),
                        request.state(),
                        request.postalCode(),
                        request.country()
                )
                .orElseThrow(() -> new BadRequestException(
                        "We couldn't find that address on the map. Check the PIN code, "
                                + "or tap \"Use my current location\" while you're at the shop."
                ));
    }

    @Override
    public Optional<StoreResponse> getMyStore() {

        User user = currentUserService.getCurrentUser();

        return merchantRepository.findByUser(user)
                .flatMap(storeRepository::findByMerchant)
                .map(store -> new StoreResponse(
                        store.getId(),
                        store.getName(),
                        store.getBusinessType(),
                        store.getAddressLine1(),
                        store.getAddressLine2(),
                        store.getCity(),
                        store.getState(),
                        store.getPostalCode(),
                        store.getCountry(),
                        store.getLatitude(),
                        store.getLongitude()
                ));
    }
}