package com.campusflow.requestservice.service.impl;

import com.campusflow.requestservice.document.Attachment;
import com.campusflow.requestservice.exception.FileUploadException;
import com.campusflow.requestservice.service.StorageService;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class StorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageServiceImpl.class);

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "application/pdf"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Value("${gcp.storage.bucket-name:campusflow-attachments}")
    private String bucketName;

    @Value("${gcp.project-id:GCP_PROJECT_ID_HERE}")
    private String projectId;

    private Storage storage;

    @PostConstruct
    public void init() {
        try {
            if (projectId != null && !projectId.equals("GCP_PROJECT_ID_HERE")) {
                storage = StorageOptions.newBuilder()
                        .setProjectId(projectId)
                        .build()
                        .getService();
                log.info("Cloud Storage initialized for bucket: {}", bucketName);
            } else {
                log.warn("GCP Project ID not configured — Cloud Storage uploads disabled. " +
                         "Set GCP_PROJECT_ID environment variable to enable.");
            }
        } catch (Exception e) {
            log.warn("Failed to initialize Cloud Storage: {}. Uploads will be disabled.", e.getMessage());
        }
    }

    @Override
    public Attachment uploadFile(String requestId, MultipartFile file) {
        // Validate file
        if (file.isEmpty()) {
            throw new FileUploadException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("File size exceeds maximum allowed size of 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new FileUploadException("File type not allowed. Allowed types: jpg, jpeg, png, pdf");
        }

        String attachmentId = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String objectName = "requests/" + requestId + "/" + attachmentId + "_" + originalFilename;

        // Upload to GCS if available
        if (storage != null) {
            try {
                BlobId blobId = BlobId.of(bucketName, objectName);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                        .setContentType(contentType)
                        .build();
                storage.create(blobInfo, file.getBytes());
                log.info("File uploaded to GCS: {}/{}", bucketName, objectName);
            } catch (IOException e) {
                throw new FileUploadException("Failed to upload file to storage", e);
            }
        } else {
            log.warn("Cloud Storage not available — file metadata stored but file not persisted to GCS");
        }

        // Build attachment metadata
        Attachment attachment = new Attachment();
        attachment.setId(attachmentId);
        attachment.setFileName(originalFilename);
        attachment.setContentType(contentType);
        attachment.setFileSize(file.getSize());
        attachment.setGcsObjectName(objectName);
        attachment.setGcsBucket(bucketName);
        attachment.setUploadedAt(Instant.now());

        return attachment;
    }
}
