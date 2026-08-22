package com.campusflow.requestservice.service.impl;

import com.campusflow.requestservice.service.NotificationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class NotificationClientImpl implements NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClientImpl.class);

    private final DiscoveryClient discoveryClient;
    private final RestClient restClient;

    public NotificationClientImpl(DiscoveryClient discoveryClient, RestClient.Builder restClientBuilder) {
        this.discoveryClient = discoveryClient;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public void sendNotification(String userId, String message, String type, String requestId) {
        try {
            var instances = discoveryClient.getInstances("NOTIFICATION-SERVICE");
            if (instances.isEmpty()) {
                log.warn("No instances of NOTIFICATION-SERVICE found — notification not sent");
                return;
            }

            String baseUrl = instances.getFirst().getUri().toString();

            Map<String, String> body = Map.of(
                    "userId", userId,
                    "message", message,
                    "type", type,
                    "requestId", requestId != null ? requestId : ""
            );

            restClient.post()
                    .uri(baseUrl + "/internal/notifications")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Notification sent to user: {} — {}", userId, message);
        } catch (Exception e) {
            // Notification failures should never break the main flow
            log.error("Failed to send notification to user: {} — {}", userId, e.getMessage());
        }
    }
}
