package com.stockup.backend.domain.merchant.service.impl;

import com.stockup.backend.common.exceptions.model.ConflictException;
import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.merchant.dto.request.CreateMerchantRequest;
import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.merchant.dto.response.MerchantProfileResponse;
import com.stockup.backend.domain.merchant.exception.MerchantAlreadyExistsException;
import com.stockup.backend.domain.merchant.repository.MerchantRepository;
import com.stockup.backend.domain.merchant.service.MerchantService;
import com.stockup.backend.domain.user.entity.enums.Role;
import com.stockup.backend.domain.user.entity.User;
import com.stockup.backend.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Override
    public void registerMerchant(CreateMerchantRequest request) {

        User user = currentUserService.getCurrentUser();

        if (merchantRepository.existsByUser(user)) {
            throw new MerchantAlreadyExistsException();
        }

        // The side of the marketplace is settled at sign-up, not here. Opening a
        // shop from a shopper account would silently make one login both, which
        // is exactly what the one-time fork exists to prevent.
        if (!user.hasRole(Role.MERCHANT)) {
            throw new ConflictException(
                    "This account is registered for shopping. "
                            + "Shops are opened from a separate shopkeeper account.");
        }

        if (user.getPhone() != null && !user.getPhone().isBlank()
                && userRepository.existsByPhoneInRole(
                        user.getPhone(), user.getId(), true, Role.MERCHANT)) {
            throw new ConflictException(
                    "Another shop is already registered with that phone number. "
                            + "Use a different number for your shop.");
        }

        Merchant merchant = new Merchant(user);

        merchantRepository.save(merchant);
    }

    @Override
    public Optional<MerchantProfileResponse> getMyProfile() {

        User user = currentUserService.getCurrentUser();

        return merchantRepository.findByUser(user)
                .map(merchant -> new MerchantProfileResponse(
                        merchant.getId(),
                        merchant.getBharosaScore()
                ));
    }
}