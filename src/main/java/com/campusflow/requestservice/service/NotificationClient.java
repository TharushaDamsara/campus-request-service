package com.campusflow.requestservice.service;

/**
 * Client for sending notifications via the Notification Service (inter-service communication).
 */
public interface NotificationClient {

    void sendNotification(String userId, String message, String type, String requestId);
}
