package com.stockup.backend.domain.discovery.controller;

import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.domain.discovery.DiscoveryService;
import com.stockup.backend.domain.discovery.dto.NearbyStoreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/discovery")
@RequiredArgsConstructor
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    /**
     * Shops near a point, ordered by the ranking blend — trust first, but not
     * so far that nobody would walk there.
     */
    @GetMapping("/stores")
    public ResponseEntity<ApiResponse<List<NearbyStoreResponse>>> nearby(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5000") int radiusMeters
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.FETCHED,
                discoveryService.nearby(latitude, longitude, Math.min(radiusMeters, 50_000))
        );
    }
}
