package com.citycare.controller;

import com.citycare.dto.ComplaintResponse;
import com.citycare.entity.User;
import com.citycare.exception.UnauthorizedException;
import com.citycare.repository.UserRepository;
import com.citycare.service.ComplaintService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/officer")
@PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
public class OfficerController {

    private final ComplaintService complaintService;
    private final UserRepository userRepository;

    public OfficerController(ComplaintService complaintService, UserRepository userRepository) {
        this.complaintService = complaintService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(Authentication auth) {
        if (auth == null) throw new UnauthorizedException("User is not authenticated.");
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedException("User not found in system."));
    }

    @GetMapping("/complaints")
    public ResponseEntity<List<ComplaintResponse>> getAssignedComplaints(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String search,
            Authentication auth) {

        User officer = getAuthenticatedUser(auth);
        List<ComplaintResponse> complaints = complaintService.getComplaintsForOfficer(officer);

        List<ComplaintResponse> filtered = complaints.stream()
                .filter(c -> status == null || status.isBlank() || c.getStatus().equalsIgnoreCase(status))
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
}
