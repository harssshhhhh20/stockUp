package com.stockup.backend.domain.notification.repository;

import com.stockup.backend.domain.notification.entity.Notification;
import com.stockup.backend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, java.util.UUID> {

    Page<Notification> findAllByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

    long countByRecipientAndReadFalse(User recipient);
}
