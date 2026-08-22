package com.campusflow.requestservice.controller;

import com.campusflow.requestservice.document.Attachment;
import com.campusflow.requestservice.document.Comment;
import com.campusflow.requestservice.document.ServiceRequest;
import com.campusflow.requestservice.dto.request.*;
import com.campusflow.requestservice.dto.response.ApiResponse;
import com.campusflow.requestservice.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@Tag(name = "Requests", description = "Service request management")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @Operation(summary = "Create a new service request")
    public ResponseEntity<ApiResponse<ServiceRequest>> createRequest(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody CreateRequestDto dto) {
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        ServiceRequest request = requestService.createRequest(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Request created successfully", request));
    }

    @GetMapping
    @Operation(summary = "List service requests")
    public ResponseEntity<ApiResponse<Page<ServiceRequest>>> getRequests(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        Page<ServiceRequest> requests = requestService.getRequests(userId, userRole, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Requests retrieved", requests));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get request by ID")
    public ResponseEntity<ApiResponse<ServiceRequest>> getRequestById(@PathVariable String id) {
        ServiceRequest request = requestService.getRequestById(id);
        return ResponseEntity.ok(ApiResponse.success("Request retrieved", request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update request details")
    public ResponseEntity<ApiResponse<ServiceRequest>> updateRequest(
            @PathVariable String id,
            @Valid @RequestBody UpdateRequestDto dto) {
        ServiceRequest request = requestService.updateRequest(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Request updated successfully", request));
    }

    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assign staff to request")
    public ResponseEntity<ApiResponse<ServiceRequest>> assignRequest(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable String id,
            @Valid @RequestBody AssignRequestDto dto) {
        if (!"STAFF".equals(userRole) && !"ADMIN".equals(userRole)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Forbidden — staff or admin access required"));
        }
        ServiceRequest request = requestService.assignRequest(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Request assigned successfully", request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update request status")
    public ResponseEntity<ApiResponse<ServiceRequest>> updateStatus(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable String id,
            @Valid @RequestBody UpdateStatusDto dto) {
        if (!"STAFF".equals(userRole) && !"ADMIN".equals(userRole)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Forbidden — staff or admin access required"));
        }
        ServiceRequest request = requestService.updateStatus(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Request status updated", request));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add comment to request")
    public ResponseEntity<ApiResponse<Comment>> addComment(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable String id,
            @Valid @RequestBody AddCommentDto dto) {
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        Comment comment = requestService.addComment(id, userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added successfully", comment));
    }

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload attachment to request")
    public ResponseEntity<ApiResponse<Attachment>> uploadAttachment(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        Attachment attachment = requestService.uploadAttachment(id, userId, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attachment uploaded successfully", attachment));
    }

    @GetMapping("/{id}/attachments")
    @Operation(summary = "List request attachments")
    public ResponseEntity<ApiResponse<List<Attachment>>> getAttachments(@PathVariable String id) {
        List<Attachment> attachments = requestService.getAttachments(id);
        return ResponseEntity.ok(ApiResponse.success("Attachments retrieved", attachments));
    }
}
