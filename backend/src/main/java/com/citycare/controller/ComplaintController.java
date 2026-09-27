package com.citycare.controller;

import com.citycare.dto.ComplaintRequest;
import com.citycare.dto.ComplaintResponse;
import com.citycare.dto.StatusUpdateRequest;
import com.citycare.entity.User;
import com.citycare.exception.UnauthorizedException;
import com.citycare.repository.UserRepository;
import com.citycare.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final UserRepository userRepository;

    public ComplaintController(ComplaintService complaintService, UserRepository userRepository) {
        this.complaintService = complaintService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(Authentication auth) {
        if (auth == null) {
            throw new UnauthorizedException("User is not authenticated.");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedException("User not found in system."));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComplaintResponse> submitComplaint(
            @Valid @ModelAttribute ComplaintRequest request,
            Authentication auth) {
        User user = getAuthenticatedUser(auth);
        ComplaintResponse response = complaintService.createComplaint(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ComplaintResponse>> getComplaints(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String search,
            Authentication auth) {

        User user = getAuthenticatedUser(auth);
        List<ComplaintResponse> complaints;

        if (user.getRole().equals("ADMIN")) {
            complaints = complaintService.getAllComplaints();
        } else if (user.getRole().equals("OFFICER")) {
            complaints = complaintService.getComplaintsForOfficer(user);
        } else {
            complaints = complaintService.getComplaintsForCitizen(user.getId());
        }

        // Apply filters
        List<ComplaintResponse> filtered = complaints.stream()
                .filter(c -> status == null || status.isBlank() || c.getStatus().equalsIgnoreCase(status))
                .filter(c -> category == null || category.isBlank() || c.getCategory().equalsIgnoreCase(category))
                .filter(c -> priority == null || priority.isBlank() || c.getPriority().equalsIgnoreCase(priority))
                .filter(c -> {
                    if (search == null || search.isBlank()) return true;
                    String s = search.toLowerCase();
                    boolean matchDesc = c.getDescription() != null && c.getDescription().toLowerCase().contains(s);
                    boolean matchId = c.getComplaintId() != null && c.getComplaintId().toLowerCase().contains(s);
                    boolean matchCat = c.getCategory() != null && c.getCategory().toLowerCase().contains(s);
                    return matchDesc || matchId || matchCat;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponse> getComplaintById(@PathVariable Long id) {
        ComplaintResponse response = complaintService.getComplaintById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ComplaintResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            Authentication auth) {
        User user = getAuthenticatedUser(auth);
        ComplaintResponse response = complaintService.updateStatus(id, request.getStatus(), request.getRemarks(), user);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{id}/resolution-proof", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComplaintResponse> uploadResolutionProof(
            @PathVariable Long id,
            @RequestParam("proof") MultipartFile proof,
            @RequestParam(value = "remarks", required = false) String remarks,
            Authentication auth) {
        User user = getAuthenticatedUser(auth);
        ComplaintResponse response = complaintService.uploadResolutionProof(id, proof, remarks, user);
        return ResponseEntity.ok(response);
    }
}
