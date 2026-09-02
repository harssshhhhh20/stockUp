package com.stockup.backend.infrastructure.notification.push;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Push via Expo's service.
 *
 * Expo relays to APNs and FCM on our behalf, so development and Expo Go need no
 * Apple or Google credentials at all. A production standalone build still needs
 * those configured in the Expo project — but no code here changes when they are.
 *
 * Sending is async and failure-tolerant on purpose: a push is a courtesy copy of
 * something already in the in-app feed, and must never be able to fail a
 * reservation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushService {

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    private final PushTokenRepository tokenRepository;
    private final RestClient restClient;

    @Transactional
    public void register(UUID userId, String token, String platform) {
        tokenRepository.findByToken(token).ifPresentOrElse(
                existing -> {
                    // Same device, possibly a different account — rebind it
                    // rather than leaving a stale owner receiving their alerts.
                    existing.reactivateFor(userId);
                    tokenRepository.save(existing);
                },
                () -> tokenRepository.save(PushToken.of(userId, token, platform))
        );
    }

    @Async
    @Transactional
    public void send(UUID userId, String title, String body, Map<String, Object> data) {
        List<PushToken> tokens = tokenRepository.findByUserIdAndActiveTrue(userId);
        if (tokens.isEmpty()) return;

        for (PushToken t : tokens) {
            try {
                var payload = Map.of(
                        "to", t.getToken(),
                        "title", title,
                        "body", body,
                        "sound", "default",
                        "data", data == null ? Map.of() : data
                );

                String response = restClient.post()
                        .uri(EXPO_PUSH_URL)
                        .header("accept", "application/json")
                        .header("content-type", "application/json")
                        .body(payload)
                        .retrieve()
                        .body(String.class);

                // Expo reports dead devices in the response body rather than by
                // status, so a 200 is not proof of delivery.
                if (response != null && response.contains("DeviceNotRegistered")) {
                    t.deactivate();
                    log.info("Deactivated dead push token for user {}", userId);
                } else {
                    t.markUsed();
                }
                tokenRepository.save(t);

            } catch (Exception ex) {
                log.warn("Push to user {} failed: {}", userId, ex.getMessage());
            }
        }
    }
}
