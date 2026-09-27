package com.citycare.repository;

import com.citycare.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByComplaintId(String complaintId);

    List<Complaint> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Complaint> findByDepartmentIdOrderByCreatedAtDesc(Long departmentId);

    List<Complaint> findByOfficerIdOrderByCreatedAtDesc(Long officerId);

    List<Complaint> findAllByOrderByCreatedAtDesc();

    long countByStatus(String status);

    long countByPriority(String priority);

    long countByCategory(String category);

    @Query("SELECT c.category, COUNT(c) FROM Complaint c GROUP BY c.category")
    List<Object[]> countComplaintsByCategory();

    @Query("SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status")
    List<Object[]> countComplaintsByStatus();

    @Query("SELECT d.name, COUNT(c) FROM Complaint c JOIN c.department d GROUP BY d.name")
    List<Object[]> countComplaintsByDepartment();

    // Query for duplicate detection within category, time window and non-null coordinates
    @Query("SELECT c FROM Complaint c WHERE c.category = :category AND c.createdAt >= :since AND c.latitude IS NOT NULL AND c.longitude IS NOT NULL")
    List<Complaint> findRecentComplaintsByCategory(
            @Param("category") String category,
            @Param("since") LocalDateTime since
    );
}
