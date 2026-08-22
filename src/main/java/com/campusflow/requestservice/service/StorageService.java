package com.campusflow.requestservice.service;

import com.campusflow.requestservice.document.Attachment;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for uploading and managing files in Google Cloud Storage.
 */
public interface StorageService {

    Attachment uploadFile(String requestId, MultipartFile file);
}
