package com.campusflow.requestservice.service.impl;

import com.campusflow.requestservice.document.*;
import com.campusflow.requestservice.dto.request.*;
import com.campusflow.requestservice.exception.ResourceNotFoundException;
import com.campusflow.requestservice.repository.ServiceRequestRepository;
import com.campusflow.requestservice.service.*;
import com.campusflow.requestservice.util.RequestIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceImpl.class);

    private final ServiceRequestRepository requestRepository;
    private final StorageService storageService;
    private final AuditService auditService;
    private final NotificationClient notificationClient;
    private final RequestIdGenerator idGenerator;

    public RequestServiceImpl(ServiceRequestRepository requestRepository,
                              StorageService storageService,
                              AuditService auditService,
                              NotificationClient notificationClient,
                              RequestIdGenerator idGenerator) {
        this.requestRepository = requestRepository;
        this.storageService = storageService;
        this.auditService = auditService;
        this.notificationClient = notificationClient;
        this.idGenerator = idGenerator;
    }

    @Override
    public ServiceRequest createRequest(String userId, CreateRequestDto dto) {
        ServiceRequest request = new ServiceRequest();
        request.setId(idGenerator.generateRequestId());
        request.setStudentId(userId);
        request.setCategory(Category.valueOf(dto.getCategory().toUpperCase()));
        request.setTitle(dto.getTitle().trim());
        request.setDescription(dto.getDescription().trim());
        request.setPriority(Priority.valueOf(dto.getPriority().toUpperCase()));
        request.setStatus(RequestStatus.PENDING);

        ServiceRequest saved = requestRepository.save(request);
        log.info("Request created: {} by user: {}", saved.getId(), userId);

        auditService.logEvent("REQUEST_CREATED", userId, saved.getId(),
                "New service request created",
                Map.of("category", dto.getCategory(), "priority", dto.getPriority()));

        return saved;
    }

    @Override
    public Page<ServiceRequest> getRequests(String userId, String userRole, String status, Pageable pageable) {
        if ("STUDENT".equals(userRole)) {
            // Students only see their own requests
            return requestRepository.findByStudentId(userId, pageable);
        }

        if (status != null && !status.isBlank()) {
            return requestRepository.findByStatus(RequestStatus.valueOf(status.toUpperCase()), pageable);
        }

        // STAFF and ADMIN see all
        return requestRepository.findAll(pageable);
    }

    @Override
    public ServiceRequest getRequestById(String id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));
    }

    @Override
    public ServiceRequest updateRequest(String id, UpdateRequestDto dto) {
        ServiceRequest request = getRequestById(id);

        if (dto.getTitle() != null) {
            request.setTitle(dto.getTitle().trim());
        }
        if (dto.getDescription() != null) {
            request.setDescription(dto.getDescription().trim());
        }
        if (dto.getPriority() != null) {
            request.setPriority(Priority.valueOf(dto.getPriority().toUpperCase()));
        }
        if (dto.getCategory() != null) {
            request.setCategory(Category.valueOf(dto.getCategory().toUpperCase()));
        }

        ServiceRequest updated = requestRepository.save(request);
        log.info("Request updated: {}", id);
        return updated;
    }

    @Override
    public ServiceRequest assignRequest(String id, AssignRequestDto dto) {
        ServiceRequest request = getRequestById(id);
        request.setAssignedStaffId(dto.getStaffId());
        request.setStatus(RequestStatus.ASSIGNED);

        ServiceRequest updated = requestRepository.save(request);
        log.info("Request {} assigned to staff: {}", id, dto.getStaffId());

        auditService.logEvent("REQUEST_ASSIGNED", dto.getStaffId(), id,
                "Request assigned to staff",
                Map.of("staffId", dto.getStaffId()));

        // Notify the student
        notificationClient.sendNotification(
                request.getStudentId(),
                "Your request " + id + " has been assigned to a staff member.",
                "REQUEST_ASSIGNED",
                id
        );

        return updated;
    }

    @Override
    public ServiceRequest updateStatus(String id, UpdateStatusDto dto) {
        ServiceRequest request = getRequestById(id);
        RequestStatus oldStatus = request.getStatus();
        RequestStatus newStatus = RequestStatus.valueOf(dto.getStatus().toUpperCase());

        // Validate status transitions
        validateStatusTransition(oldStatus, newStatus);

        request.setStatus(newStatus);
        ServiceRequest updated = requestRepository.save(request);
        log.info("Request {} status changed: {} -> {}", id, oldStatus, newStatus);

        auditService.logEvent("REQUEST_STATUS_CHANGED", request.getStudentId(), id,
                "Request status changed from " + oldStatus + " to " + newStatus,
                Map.of("oldStatus", oldStatus.name(), "newStatus", newStatus.name()));

        // Determine notification message
        String message = switch (newStatus) {
            case IN_PROGRESS -> "Your request " + id + " is now being worked on.";
            case RESOLVED -> "Your request " + id + " has been resolved.";
            case REJECTED -> "Your request " + id + " has been rejected.";
            default -> "Your request " + id + " status has been updated to " + newStatus + ".";
        };

        notificationClient.sendNotification(
                request.getStudentId(),
                message,
                newStatus == RequestStatus.RESOLVED ? "REQUEST_RESOLVED" : "REQUEST_UPDATE",
                id
        );

        if (newStatus == RequestStatus.RESOLVED) {
            auditService.logEvent("REQUEST_RESOLVED", request.getStudentId(), id,
                    "Request resolved");
        }

        return updated;
    }

    @Override
    public Comment addComment(String id, String userId, AddCommentDto dto) {
        ServiceRequest request = getRequestById(id);

        Comment comment = new Comment();
        comment.setId(UUID.randomUUID().toString());
        comment.setUserId(userId);
        comment.setContent(dto.getContent().trim());
        comment.setCreatedAt(Instant.now());

        request.getComments().add(comment);
        requestRepository.save(request);
        log.info("Comment added to request {} by user {}", id, userId);

        return comment;
    }

    @Override
    public Attachment uploadAttachment(String id, String userId, MultipartFile file) {
        ServiceRequest request = getRequestById(id);

        Attachment attachment = storageService.uploadFile(id, file);
        request.getAttachments().add(attachment);
        requestRepository.save(request);

        log.info("Attachment uploaded to request {} by user {}: {}", id, userId, attachment.getFileName());

        auditService.logEvent("FILE_UPLOADED", userId, id,
                "File uploaded: " + attachment.getFileName(),
                Map.of("fileName", attachment.getFileName(), "fileSize", attachment.getFileSize()));

        return attachment;
    }

    @Override
    public List<Attachment> getAttachments(String id) {
        ServiceRequest request = getRequestById(id);
        return request.getAttachments();
    }

    private void validateStatusTransition(RequestStatus from, RequestStatus to) {
        boolean valid = switch (from) {
            case PENDING -> to == RequestStatus.ASSIGNED || to == RequestStatus.REJECTED;
            case ASSIGNED -> to == RequestStatus.IN_PROGRESS;
            case IN_PROGRESS -> to == RequestStatus.RESOLVED || to == RequestStatus.REJECTED;
            default -> false;
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid status transition from " + from + " to " + to);
        }
    }
}
