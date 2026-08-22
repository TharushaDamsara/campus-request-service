package com.campusflow.requestservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AssignRequestDto {

    @NotBlank(message = "Staff ID is required")
    private String staffId;

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }
}
