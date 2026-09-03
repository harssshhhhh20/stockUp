package com.stockup.backend.domain.user.controller;

import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.merchant.repository.MerchantRepository;
import com.stockup.backend.domain.store.entity.Store;
import com.stockup.backend.domain.store.repository.StoreRepository;
import com.stockup.backend.domain.user.dto.UpdateProfileRequest;
import com.stockup.backend.domain.user.dto.UserProfileResponse;
import com.stockup.backend.domain.user.entity.User;
import com.stockup.backend.domain.user.entity.enums.Role;
import com.stockup.backend.common.exceptions.model.ConflictException;
import com.stockup.backend.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final StoreRepository storeRepository;

    /**
     * The single call the app makes on open to decide where to send someone:
     * choose a role, finish a profile, set up a shop, or straight in.
     *
     * Answering all of that in one response is deliberate — onboarding routing
     * assembled from three separate requests flickers between states while they
     * resolve.
     */
    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<UserProfileResponse>> me() {
        return ApiResponseFactory.success(
                ResponseMessage.FETCHED,
                profileOf(currentUserService.getCurrentUser())
        );
    }

    @PatchMapping("/me")
    @Transactional
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMe(
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        User user = currentUserService.getCurrentUser();

        // A number identifies one person per side of the marketplace. Two shops
        // sharing a number, or two shoppers, is the sockpuppet case; a shop and
        // a shopper sharing one is just somebody who does both. Checked here so
        // the caller gets a clear conflict rather than a constraint violation.
        boolean asMerchant = user.hasRole(Role.MERCHANT);
        if (userRepository.existsByPhoneInRole(
                request.phone().trim(), user.getId(), asMerchant, Role.MERCHANT)) {
            throw new ConflictException(asMerchant
                    ? "Another shop is already registered with that phone number."
                    : "That phone number is already linked to another account.");
        }

        user.updateProfile(request.firstName(), request.lastName(), request.phone());
        userRepository.save(user);

        return ApiResponseFactory.success(ResponseMessage.UPDATED, profileOf(user));
    }

    private UserProfileResponse profileOf(User user) {
        Optional<Merchant> merchant = merchantRepository.findByUser(user);
        Optional<Store> store = merchant.flatMap(storeRepository::findByMerchant);

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRoles().stream().map(Role::name).collect(Collectors.toSet()),
                user.isProfileComplete(),
                merchant.isPresent(),
                store.isPresent(),
                merchant.map(Merchant::getId).orElse(null),
                merchant.map(Merchant::getBharosaScore).orElse(null),
                store.map(Store::getId).orElse(null),
                store.map(Store::getName).orElse(null)
        );
    }
}
