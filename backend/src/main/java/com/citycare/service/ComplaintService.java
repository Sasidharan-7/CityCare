package com.citycare.service;

import com.citycare.dto.AIAnalysisResponse;
import com.citycare.dto.AnalyticsResponse;
import com.citycare.dto.ComplaintRequest;
import com.citycare.dto.ComplaintResponse;
import com.citycare.entity.*;
import com.citycare.exception.BadRequestException;
import com.citycare.exception.ResourceNotFoundException;
import com.citycare.repository.ComplaintRepository;
import com.citycare.repository.ComplaintUpdateRepository;
import com.citycare.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintUpdateRepository updateRepository;
    private final UserRepository userRepository;
    private final DepartmentService departmentService;
    private final FileStorageService fileStorageService;
    private final AIServiceClient aiServiceClient;
    private final DuplicateDetectionService duplicateDetectionService;
    private final NotificationService notificationService;

    public ComplaintService(ComplaintRepository complaintRepository,
                            ComplaintUpdateRepository updateRepository,
                            UserRepository userRepository,
                            DepartmentService departmentService,
                            FileStorageService fileStorageService,
                            AIServiceClient aiServiceClient,
                            DuplicateDetectionService duplicateDetectionService,
                            NotificationService notificationService) {
        this.complaintRepository = complaintRepository;
        this.updateRepository = updateRepository;
        this.userRepository = userRepository;
        this.departmentService = departmentService;
        this.fileStorageService = fileStorageService;
        this.aiServiceClient = aiServiceClient;
        this.duplicateDetectionService = duplicateDetectionService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ComplaintResponse createComplaint(User user, ComplaintRequest request) {
        // 1. Store issue image
        String imageUrl = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imageUrl = fileStorageService.storeFile(request.getImage());
        }

        // 2. AI Analysis & Priority Calculation (Phase 6 & 7)
        String category = request.getCategory().toUpperCase();
        AIAnalysisResponse aiAnalysis = aiServiceClient.analyzeComplaint(category, request.getDescription(), imageUrl);

        String priority = (request.getPriority() != null && !request.getPriority().isBlank())
                ? request.getPriority().toUpperCase()
                : aiAnalysis.getPriority();

        // 3. Department Routing (Phase 8)
        Department department = departmentService.routeCategoryToDepartment(category);

        // 4. Duplicate Detection (Phase 11)
        Optional<Complaint> duplicate = duplicateDetectionService.detectDuplicate(
                category, request.getLatitude(), request.getLongitude()
        );

        // 5. Build Complaint Entity
        Complaint complaint = new Complaint();
        complaint.setUser(user);
        complaint.setCategory(category);
        complaint.setDescription(request.getDescription());
        complaint.setImageUrl(imageUrl);
        complaint.setLatitude(request.getLatitude());
        complaint.setLongitude(request.getLongitude());
        complaint.setPriority(priority);
        complaint.setStatus("ASSIGNED");
        complaint.setDepartment(department);

        if (duplicate.isPresent()) {
            complaint.setPossibleDuplicate(true);
            complaint.setPrimaryComplaintId(duplicate.get().getId());
        }

        // Auto-assign first officer from this department if available
        if (department != null) {
            List<User> officers = userRepository.findByDepartmentId(department.getId());
            if (!officers.isEmpty()) {
                complaint.setOfficer(officers.get(0));
            }
        }

        // Temporary unique ID
        complaint.setComplaintId("TMP-" + UUID.randomUUID().toString().substring(0, 8));
        Complaint saved = complaintRepository.save(complaint);

        // Set official readable complaint ID (e.g. CC1001)
        String readableId = "CC" + (1000 + saved.getId());
        saved.setComplaintId(readableId);

        // Initial timeline update
        ComplaintUpdate initialUpdate = new ComplaintUpdate(
                saved, "PENDING", "Complaint submitted by citizen.", user
        );
        updateRepository.save(initialUpdate);

        if (department != null) {
            ComplaintUpdate assignUpdate = new ComplaintUpdate(
                    saved, "ASSIGNED", "Automatically routed to " + department.getName(), null
            );
            updateRepository.save(assignUpdate);
        }

        complaintRepository.save(saved);

        // In-App Notifications (Phase 12)
        notificationService.createNotification(
                user, saved, "Complaint #" + readableId + " submitted successfully."
        );
        if (department != null) {
            notificationService.createNotification(
                    user, saved, "Complaint #" + readableId + " routed to " + department.getName() + "."
            );
        }

        return mapToResponse(saved);
    }

    public ComplaintResponse getComplaintById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));
        return mapToResponse(complaint);
    }

    public List<ComplaintResponse> getComplaintsForCitizen(Long userId) {
        return complaintRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ComplaintResponse> getComplaintsForOfficer(User officer) {
        List<Complaint> list;
        if (officer.getDepartment() != null) {
            // All complaints in officer's department
            list = complaintRepository.findByDepartmentIdOrderByCreatedAtDesc(officer.getDepartment().getId());
        } else {
            list = complaintRepository.findByOfficerIdOrderByCreatedAtDesc(officer.getId());
        }
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComplaintResponse updateStatus(Long complaintId, String newStatus, String remarks, User officer) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        complaint.setStatus(newStatus.toUpperCase());
        complaint.setUpdatedAt(LocalDateTime.now());
        if (officer != null && complaint.getOfficer() == null) {
            complaint.setOfficer(officer);
        }

        ComplaintUpdate update = new ComplaintUpdate(complaint, newStatus, remarks, officer);
        updateRepository.save(update);

        Complaint updated = complaintRepository.save(complaint);

        // Notify Citizen (Phase 12)
        notificationService.createNotification(
                complaint.getUser(),
                updated,
                "Complaint #" + updated.getComplaintId() + " status is now " + newStatus + "."
        );

        return mapToResponse(updated);
    }

    @Transactional
    public ComplaintResponse uploadResolutionProof(Long complaintId, MultipartFile proofImage, String remarks, User officer) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        if (proofImage == null || proofImage.isEmpty()) {
            throw new BadRequestException("Resolution proof photo is required.");
        }

        String proofUrl = fileStorageService.storeFile(proofImage);
        complaint.setResolutionProofUrl(proofUrl);
        complaint.setResolutionRemarks(remarks);
        complaint.setStatus("RESOLVED");
        complaint.setUpdatedAt(LocalDateTime.now());
        if (officer != null) {
            complaint.setOfficer(officer);
        }

        ComplaintUpdate update = new ComplaintUpdate(
                complaint, "RESOLVED", "Issue resolved. Proof photo uploaded by officer. " + (remarks != null ? remarks : ""), officer
        );
        updateRepository.save(update);

        Complaint updated = complaintRepository.save(complaint);

        // Notify Citizen
        notificationService.createNotification(
                complaint.getUser(),
                updated,
                "Complaint #" + updated.getComplaintId() + " has been resolved. You can now view resolution proof."
        );

        return mapToResponse(updated);
    }

    public AnalyticsResponse getAnalytics() {
        AnalyticsResponse resp = new AnalyticsResponse();

        resp.setTotalComplaints(complaintRepository.count());
        resp.setPending(complaintRepository.countByStatus("PENDING"));
        resp.setAssigned(complaintRepository.countByStatus("ASSIGNED"));
        resp.setInProgress(complaintRepository.countByStatus("IN_PROGRESS"));
        resp.setResolved(complaintRepository.countByStatus("RESOLVED"));
        resp.setClosed(complaintRepository.countByStatus("CLOSED"));
        resp.setCritical(complaintRepository.countByPriority("CRITICAL"));

        resp.setTotalUsers(userRepository.countByRole("CITIZEN"));
        resp.setTotalOfficers(userRepository.countByRole("OFFICER"));

        // By Category
        Map<String, Long> byCategory = new HashMap<>();
        complaintRepository.countComplaintsByCategory().forEach(row -> {
            byCategory.put((String) row[0], ((Number) row[1]).longValue());
        });
        resp.setByCategory(byCategory);

        // By Status
        Map<String, Long> byStatus = new HashMap<>();
        complaintRepository.countComplaintsByStatus().forEach(row -> {
            byStatus.put((String) row[0], ((Number) row[1]).longValue());
        });
        resp.setByStatus(byStatus);

        // By Department
        Map<String, Long> byDepartment = new HashMap<>();
        complaintRepository.countComplaintsByDepartment().forEach(row -> {
            byDepartment.put((String) row[0], ((Number) row[1]).longValue());
        });
        resp.setByDepartment(byDepartment);

        // List of all complaints with coordinates for admin map
        resp.setComplaints(getAllComplaints());

        return resp;
    }

    public ComplaintResponse mapToResponse(Complaint c) {
        ComplaintResponse dto = new ComplaintResponse();
        dto.setId(c.getId());
        dto.setComplaintId(c.getComplaintId());
        dto.setCategory(c.getCategory());
        dto.setDescription(c.getDescription());
        dto.setImageUrl(c.getImageUrl());
        dto.setLatitude(c.getLatitude());
        dto.setLongitude(c.getLongitude());
        dto.setPriority(c.getPriority());
        dto.setStatus(c.getStatus());
        dto.setResolutionProofUrl(c.getResolutionProofUrl());
        dto.setResolutionRemarks(c.getResolutionRemarks());
        dto.setPossibleDuplicate(c.getPossibleDuplicate());
        dto.setPrimaryComplaintId(c.getPrimaryComplaintId());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());

        if (c.getUser() != null) {
            dto.setUserId(c.getUser().getId());
            dto.setUserName(c.getUser().getName());
            dto.setUserPhone(c.getUser().getPhone());
        }

        if (c.getDepartment() != null) {
            dto.setDepartmentId(c.getDepartment().getId());
            dto.setDepartmentName(c.getDepartment().getName());
        }

        if (c.getOfficer() != null) {
            dto.setOfficerId(c.getOfficer().getId());
            dto.setOfficerName(c.getOfficer().getName());
        }

        // Timeline history
        List<ComplaintUpdate> updates = updateRepository.findByComplaintIdOrderByCreatedAtAsc(c.getId());
        dto.setUpdates(updates.stream().map(u -> new ComplaintResponse.ComplaintUpdateDto(
                u.getId(),
                u.getStatus(),
                u.getRemarks(),
                u.getUpdatedBy() != null ? u.getUpdatedBy().getName() : "System",
                u.getCreatedAt()
        )).collect(Collectors.toList()));

        return dto;
    }
}
