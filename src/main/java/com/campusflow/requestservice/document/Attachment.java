package com.campusflow.requestservice.document;

import java.time.Instant;

public class Attachment {

    private String id;
    private String fileName;
    private String contentType;
    private long fileSize;
    private String gcsObjectName;
    private String gcsBucket;
    private Instant uploadedAt;

    public Attachment() {
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getGcsObjectName() { return gcsObjectName; }
    public void setGcsObjectName(String gcsObjectName) { this.gcsObjectName = gcsObjectName; }

    public String getGcsBucket() { return gcsBucket; }
    public void setGcsBucket(String gcsBucket) { this.gcsBucket = gcsBucket; }

    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
}
