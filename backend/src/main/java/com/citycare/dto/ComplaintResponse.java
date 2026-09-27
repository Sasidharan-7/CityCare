package com.citycare.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ComplaintResponse {
    private Long id;
    private String complaintId;
    private Long userId;
    private String userName;
    private String userPhone;
    private String category;
    private String description;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private String priority;
    private String status;
    private Long departmentId;
    private String departmentName;
    private Long officerId;
    private String officerName;
    private String resolutionProofUrl;
    private String resolutionRemarks;
    private Boolean possibleDuplicate;
    private Long primaryComplaintId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ComplaintUpdateDto> updates;

    public static class ComplaintUpdateDto {
        private Long id;
        private String status;
        private String remarks;
        private String updatedByName;
        private LocalDateTime createdAt;

        public ComplaintUpdateDto() {}

        public ComplaintUpdateDto(Long id, String status, String remarks, String updatedByName, LocalDateTime createdAt) {
            this.id = id;
            this.status = status;
            this.remarks = remarks;
            this.updatedByName = updatedByName;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
        public String getUpdatedByName() { return updatedByName; }
        public void setUpdatedByName(String updatedByName) { this.updatedByName = updatedByName; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public ComplaintResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public Long getOfficerId() { return officerId; }
    public void setOfficerId(Long officerId) { this.officerId = officerId; }

    public String getOfficerName() { return officerName; }
    public void setOfficerName(String officerName) { this.officerName = officerName; }

    public String getResolutionProofUrl() { return resolutionProofUrl; }
    public void setResolutionProofUrl(String resolutionProofUrl) { this.resolutionProofUrl = resolutionProofUrl; }

    public String getResolutionRemarks() { return resolutionRemarks; }
    public void setResolutionRemarks(String resolutionRemarks) { this.resolutionRemarks = resolutionRemarks; }

    public Boolean getPossibleDuplicate() { return possibleDuplicate; }
    public void setPossibleDuplicate(Boolean possibleDuplicate) { this.possibleDuplicate = possibleDuplicate; }

    public Long getPrimaryComplaintId() { return primaryComplaintId; }
    public void setPrimaryComplaintId(Long primaryComplaintId) { this.primaryComplaintId = primaryComplaintId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<ComplaintUpdateDto> getUpdates() { return updates; }
    public void setUpdates(List<ComplaintUpdateDto> updates) { this.updates = updates; }
}
