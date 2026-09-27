package com.citycare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_id", unique = true, nullable = false, length = 30)
    private String complaintId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    private Double latitude;
    private Double longitude;

    @Column(nullable = false, length = 20)
    private String priority = "MEDIUM";

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "officer_id")
    private User officer;

    @Column(name = "resolution_proof_url", length = 500)
    private String resolutionProofUrl;

    @Column(name = "resolution_remarks", columnDefinition = "TEXT")
    private String resolutionRemarks;

    @Column(name = "is_possible_duplicate")
    private Boolean possibleDuplicate = false;

    @Column(name = "primary_complaint_id")
    private Long primaryComplaintId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<ComplaintUpdate> updates = new ArrayList<>();

    public Complaint() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public User getOfficer() { return officer; }
    public void setOfficer(User officer) { this.officer = officer; }

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

    public List<ComplaintUpdate> getUpdates() { return updates; }
    public void setUpdates(List<ComplaintUpdate> updates) { this.updates = updates; }

    public void addUpdate(ComplaintUpdate update) {
        updates.add(update);
        update.setComplaint(this);
    }
}
