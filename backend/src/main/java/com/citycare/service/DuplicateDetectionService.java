package com.citycare.service;

import com.citycare.entity.Complaint;
import com.citycare.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DuplicateDetectionService {

    private final ComplaintRepository complaintRepository;
    private final double distanceThresholdMeters;
    private final int windowHours;

    public DuplicateDetectionService(
            ComplaintRepository complaintRepository,
            @Value("${citycare.duplicate.distance-meters:100.0}") double distanceThresholdMeters,
            @Value("${citycare.duplicate.window-hours:48}") int windowHours) {
        this.complaintRepository = complaintRepository;
        this.distanceThresholdMeters = distanceThresholdMeters;
        this.windowHours = windowHours;
    }

    /**
     * Checks if a recent complaint exists in the same category within distance & time window.
     * Returns Optional with primary complaint if duplicate found, empty otherwise.
     */
    public Optional<Complaint> detectDuplicate(String category, Double latitude, Double longitude) {
        if (latitude == null || longitude == null || category == null) {
            return Optional.empty();
        }

        LocalDateTime since = LocalDateTime.now().minusHours(windowHours);
        List<Complaint> candidates = complaintRepository.findRecentComplaintsByCategory(category, since);

        for (Complaint candidate : candidates) {
            if (candidate.getLatitude() != null && candidate.getLongitude() != null) {
                double distance = calculateHaversineDistance(
                        latitude, longitude,
                        candidate.getLatitude(), candidate.getLongitude()
                );

                if (distance <= distanceThresholdMeters) {
                    return Optional.of(candidate);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Calculates great-circle distance between two GPS points using Haversine formula in meters.
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_METERS = 6371000.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }
}
