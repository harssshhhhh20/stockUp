package com.stockup.backend.domain.notification.controller;

import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.domain.notification.dto.NotificationResponse;
import com.stockup.backend.domain.notification.dto.UnreadCountResponse;
import com.stockup.backend.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.FETCHED,
                notificationService.getMyNotifications(pageable)
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount() {
        return ApiResponseFactory.success(
                ResponseMessage.FETCHED,
                new UnreadCountResponse(notificationService.getUnreadCount())
        );
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markRead(
            @PathVariable UUID notificationId
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.UPDATED,
                notificationService.markRead(notificationId)
        );
    }
}
