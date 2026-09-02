package com.stockup.backend.infrastructure.notification.push.controller;

import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.infrastructure.notification.push.PushService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
public class PushController {

    private final PushService pushService;
    private final CurrentUserService currentUserService;

    public record RegisterPushTokenRequest(
            @NotBlank(message = "Token is required.") String token,
            String platform
    ) {
    }

    /** The app calls this after the user grants notification permission. */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @RequestBody RegisterPushTokenRequest request
    ) {
        pushService.register(
                currentUserService.getCurrentUser().getId(),
                request.token(),
                request.platform()
        );
        return ApiResponseFactory.success(ResponseMessage.CREATED, null);
    }
}
