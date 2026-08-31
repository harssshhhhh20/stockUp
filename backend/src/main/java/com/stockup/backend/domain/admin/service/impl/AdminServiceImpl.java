package com.stockup.backend.domain.admin.service.impl;

import com.stockup.backend.domain.admin.dto.AdjustBharosaScoreRequest;
import com.stockup.backend.domain.admin.dto.MerchantSummaryResponse;
import com.stockup.backend.domain.admin.dto.StoreSummaryResponse;
import com.stockup.backend.domain.admin.exception.UserNotFoundException;
import com.stockup.backend.domain.admin.service.AdminService;
import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.merchant.exception.MerchantNotFoundException;
import com.stockup.backend.domain.merchant.repository.MerchantRepository;
import com.stockup.backend.domain.merchant.service.BharosaScoreService;
import com.stockup.backend.domain.reservation.dto.ReservationResponse;
import com.stockup.backend.domain.reservation.entity.enums.ReservationStatus;
import com.stockup.backend.domain.reservation.mapper.ReservationMapper;
import com.stockup.backend.domain.reservation.repository.ReservationRepository;
import com.stockup.backend.domain.store.entity.Store;
import com.stockup.backend.domain.store.repository.StoreRepository;
import com.stockup.backend.domain.user.entity.User;
import com.stockup.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final MerchantRepository merchantRepository;
    private final StoreRepository storeRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final UserRepository userRepository;
    private final BharosaScoreService bharosaScoreService;

    @Override
    @Transactional(readOnly = true)
    public Page<MerchantSummaryResponse> getMerchants(Pageable pageable) {
        return merchantRepository.findAll(pageable)
                .map(merchant -> new MerchantSummaryResponse(
                        merchant.getId(),
                        merchant.getUser().getEmail(),
                        merchant.getBharosaScore(),
                        merchant.getCreatedAt()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StoreSummaryResponse> getStores(Pageable pageable) {
        return storeRepository.findAll(pageable)
                .map(store -> new StoreSummaryResponse(
                        store.getId(),
                        store.getName(),
                        store.getBusinessType(),
                        store.getMerchant().getUser().getEmail(),
                        store.getCity()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservations(ReservationStatus status, Pageable pageable) {

        ReservationStatus effectiveStatus =
                status == null ? ReservationStatus.ACTIVE : status;

        return reservationRepository
                .findAllByStatus(effectiveStatus, pageable)
                .map(reservationMapper::toResponse);
    }

    @Override
    public void adjustBharosaScore(UUID merchantId, AdjustBharosaScoreRequest request) {

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(MerchantNotFoundException::new);

        bharosaScoreService.adjust(
                merchant,
                request.delta(),
                "Admin adjustment: " + request.reason()
        );
    }

    @Override
    public void suspendUser(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        user.suspend();

        userRepository.save(user);
    }
}
