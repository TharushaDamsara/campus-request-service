package com.campusflow.requestservice.service;

import java.util.Map;

/**
 * Service for writing audit events to Google Cloud Firestore.
 */
public interface AuditService {

    void logEvent(String eventType, String userId, String requestId, String description, Map<String, Object> metadata);

    default void logEvent(String eventType, String userId, String requestId, String description) {
        logEvent(eventType, userId, requestId, description, null);
    }
}
