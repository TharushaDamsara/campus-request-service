package com.campusflow.requestservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddCommentDto {

    @NotBlank(message = "Comment content is required")
    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
