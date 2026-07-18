package com.stockup.backend.domain.broadcast.controller;

import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.domain.broadcast.dto.MarkBroadcastViewedRequest;
import com.stockup.backend.domain.broadcast.service.BroadcastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/broadcasts")
@RequiredArgsConstructor
public class BroadcastController {

    private final BroadcastService broadcastService;

    @PostMapping("/view")
    public ResponseEntity<ApiResponse<Void>> markViewed(
            @Valid @RequestBody MarkBroadcastViewedRequest request
    ) {

        broadcastService.markViewed(request.broadcastRecipientId());

        return ApiResponseFactory.success(
                ResponseMessage.UPDATED,
                null
        );
    }
}