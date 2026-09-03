package com.stockup.backend.domain.notification.service.impl;

import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.notification.dto.NotificationResponse;
import com.stockup.backend.domain.notification.entity.Notification;
import com.stockup.backend.domain.notification.entity.enums.NotificationType;
import com.stockup.backend.domain.notification.exception.NotificationAccessDeniedException;
import com.stockup.backend.domain.notification.exception.NotificationNotFoundException;
import com.stockup.backend.domain.notification.mapper.NotificationMapper;
import com.stockup.backend.domain.notification.repository.NotificationRepository;
import com.stockup.backend.domain.notification.service.NotificationService;
import com.stockup.backend.domain.user.entity.User;
import com.stockup.backend.infrastructure.notification.push.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final CurrentUserService currentUserService;
    private final PushService pushService;

    @Override
    public void notify(
            User recipient,
            NotificationType type,
            String title,
            String message,
            UUID referenceId
    ) {
        Notification notification = Notification.create(
                recipient,
                type,
                title,
                message,
                referenceId
        );

        notificationRepository.save(notification);

        // Mirror it to the device so people hear about it when the app is closed.
        // Email is deliberately NOT a channel here: the only mail StockUp sends
        // is the sign-in code. Reservation traffic is high-frequency and would
        // turn every shopper's inbox into a log. Push is async and
        // failure-tolerant — the in-app feed is the source of truth, and a dead
        // device must never affect the thing that was actually notified about.
        pushService.send(
                recipient.getId(),
                title,
                message,
                referenceId == null
                        ? java.util.Map.of("type", type.name())
                        : java.util.Map.of("type", type.name(), "referenceId", referenceId.toString())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {

        User currentUser = currentUserService.getCurrentUser();

        return notificationRepository
                .findAllByRecipientOrderByCreatedAtDesc(currentUser, pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User currentUser = currentUserService.getCurrentUser();
        return notificationRepository.countByRecipientAndReadFalse(currentUser);
    }

    @Override
    public NotificationResponse markRead(UUID notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found."
                ));

        User currentUser = currentUserService.getCurrentUser();

        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new NotificationAccessDeniedException(
                    "You are not authorized to access this notification."
            );
        }

        notification.markRead();

        return notificationMapper.toResponse(notification);
    }
}
