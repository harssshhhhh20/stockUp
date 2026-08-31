package com.stockup.backend.domain.admin.controller;

import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.domain.admin.dto.AdjustBharosaScoreRequest;
import com.stockup.backend.domain.admin.dto.MerchantSummaryResponse;
import com.stockup.backend.domain.admin.dto.StoreSummaryResponse;
import com.stockup.backend.domain.admin.service.AdminService;
import com.stockup.backend.domain.reservation.dto.ReservationResponse;
import com.stockup.backend.domain.reservation.entity.enums.ReservationStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/merchants")
    public ResponseEntity<ApiResponse<Page<MerchantSummaryResponse>>> getMerchants(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.FETCHED,
                adminService.getMerchants(pageable)
        );
    }

    @GetMapping("/stores")
    public ResponseEntity<ApiResponse<Page<StoreSummaryResponse>>> getStores(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.FETCHED,
                adminService.getStores(pageable)
        );
    }

    @GetMapping("/reservations")
    public ResponseEntity<ApiResponse<Page<ReservationResponse>>> getReservations(
            @RequestParam(defaultValue = "ACTIVE") ReservationStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.FETCHED,
                adminService.getReservations(status, pageable)
        );
    }

    @PostMapping("/merchants/{merchantId}/bharosa-score")
    public ResponseEntity<ApiResponse<Void>> adjustBharosaScore(
            @PathVariable UUID merchantId,
            @Valid @RequestBody AdjustBharosaScoreRequest request
    ) {
        adminService.adjustBharosaScore(merchantId, request);

        return ApiResponseFactory.success(
                ResponseMessage.UPDATED,
                null
        );
    }

    @PostMapping("/users/{userId}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendUser(
            @PathVariable UUID userId
    ) {
        adminService.suspendUser(userId);

        return ApiResponseFactory.success(
                ResponseMessage.UPDATED,
                null
        );
    }
}
