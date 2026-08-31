package com.stockup.backend.domain.admin.service;

import com.stockup.backend.domain.admin.dto.AdjustBharosaScoreRequest;
import com.stockup.backend.domain.admin.dto.MerchantSummaryResponse;
import com.stockup.backend.domain.admin.dto.StoreSummaryResponse;
import com.stockup.backend.domain.reservation.dto.ReservationResponse;
import com.stockup.backend.domain.reservation.entity.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminService {

    Page<MerchantSummaryResponse> getMerchants(Pageable pageable);

    Page<StoreSummaryResponse> getStores(Pageable pageable);

    Page<ReservationResponse> getReservations(ReservationStatus status, Pageable pageable);

    void adjustBharosaScore(UUID merchantId, AdjustBharosaScoreRequest request);

    void suspendUser(UUID userId);
}
