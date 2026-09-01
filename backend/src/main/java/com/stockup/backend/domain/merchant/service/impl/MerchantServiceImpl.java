package com.stockup.backend.domain.merchant.service.impl;

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

        user.addRole(Role.MERCHANT);
        
        userRepository.save(user);

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