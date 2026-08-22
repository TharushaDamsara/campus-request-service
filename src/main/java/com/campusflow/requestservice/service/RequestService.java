package com.campusflow.requestservice.service;

import com.campusflow.requestservice.document.Attachment;
import com.campusflow.requestservice.document.Comment;
import com.campusflow.requestservice.document.ServiceRequest;
import com.campusflow.requestservice.dto.request.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RequestService {

    ServiceRequest createRequest(String userId, CreateRequestDto dto);

    Page<ServiceRequest> getRequests(String userId, String userRole, String status, Pageable pageable);

    ServiceRequest getRequestById(String id);

    ServiceRequest updateRequest(String id, UpdateRequestDto dto);

    ServiceRequest assignRequest(String id, AssignRequestDto dto);

    ServiceRequest updateStatus(String id, UpdateStatusDto dto);

    Comment addComment(String id, String userId, AddCommentDto dto);

    Attachment uploadAttachment(String id, String userId, MultipartFile file);

    List<Attachment> getAttachments(String id);
}
