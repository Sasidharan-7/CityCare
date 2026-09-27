package com.citycare.config;

import com.citycare.entity.*;
import com.citycare.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;
    private final ComplaintUpdateRepository updateRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(DepartmentRepository departmentRepository,
                           UserRepository userRepository,
                           ComplaintRepository complaintRepository,
                           ComplaintUpdateRepository updateRepository,
                           NotificationRepository notificationRepository,
                           PasswordEncoder passwordEncoder) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.complaintRepository = complaintRepository;
        this.updateRepository = updateRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Departments if empty
        if (departmentRepository.count() == 0) {
            List<Department> depts = Arrays.asList(
                    new Department("Road Department", "ROAD", "Handles potholes, road asphalt damages, and surface cracks"),
                    new Department("Sanitation Department", "SANITATION", "Handles solid waste accumulation, garbage clearance, and street cleanliness"),
                    new Department("Water Department", "WATER", "Handles water pipe leakages, main line bursts, and drinking water supply issues"),
                    new Department("Electricity Department", "ELECTRICITY", "Handles broken streetlights, exposed power lines, and electrical hazards"),
                    new Department("Public Works Department", "PWD", "Handles open drains, uncovered manholes, stormwater ditches, and footpaths"),
                    new Department("General Civic Department", "CIVIC", "Handles general civic amenities and miscellaneous civic issues")
            );
            departmentRepository.saveAll(depts);
        }

        // 2. Seed Users if empty
        if (userRepository.count() == 0) {
            Department roadDept = departmentRepository.findByCode("ROAD").orElse(null);
            Department sanitationDept = departmentRepository.findByCode("SANITATION").orElse(null);

            User admin = new User("Admin Officer", "admin@citycare.gov.in", "9876543210", passwordEncoder.encode("password123"), "ADMIN");
            
            User roadOfficer = new User("Rajesh Kumar", "officer.road@citycare.gov.in", "9876543211", passwordEncoder.encode("password123"), "OFFICER");
            roadOfficer.setDepartment(roadDept);

            User sanitationOfficer = new User("Sunita Sharma", "officer.sanitation@citycare.gov.in", "9876543212", passwordEncoder.encode("password123"), "OFFICER");
            sanitationOfficer.setDepartment(sanitationDept);

            User citizen = new User("Arun Patel", "citizen@gmail.com", "9876543213", passwordEncoder.encode("password123"), "CITIZEN");

            userRepository.saveAll(Arrays.asList(admin, roadOfficer, sanitationOfficer, citizen));

            // 3. Seed Sample Initial Complaints for Demo
            Department waterDept = departmentRepository.findByCode("WATER").orElse(null);
            Department pwdDept = departmentRepository.findByCode("PWD").orElse(null);

            Complaint c1 = new Complaint();
            c1.setComplaintId("CC1001");
            c1.setUser(citizen);
            c1.setCategory("POTHOLE");
            c1.setDescription("Deep pothole near MG Road metro station causing heavy traffic slow-down and bike accidents.");
            c1.setLatitude(12.9716);
            c1.setLongitude(77.5946);
            c1.setPriority("HIGH");
            c1.setStatus("IN_PROGRESS");
            c1.setDepartment(roadDept);
            c1.setOfficer(roadOfficer);
            complaintRepository.save(c1);

            ComplaintUpdate u1 = new ComplaintUpdate(c1, "PENDING", "Complaint submitted by citizen.", citizen);
            ComplaintUpdate u2 = new ComplaintUpdate(c1, "ASSIGNED", "Assigned to Road Department", null);
            ComplaintUpdate u3 = new ComplaintUpdate(c1, "IN_PROGRESS", "Asphalt repair crew dispatched to site.", roadOfficer);
            updateRepository.saveAll(Arrays.asList(u1, u2, u3));

            Complaint c2 = new Complaint();
            c2.setComplaintId("CC1002");
            c2.setUser(citizen);
            c2.setCategory("GARBAGE");
            c2.setDescription("Overflowing municipal garbage bin near 4th Cross junction. Strong odor and stray animal menace.");
            c2.setLatitude(12.9780);
            c2.setLongitude(77.6010);
            c2.setPriority("MEDIUM");
            c2.setStatus("RESOLVED");
            c2.setDepartment(sanitationDept);
            c2.setOfficer(sanitationOfficer);
            c2.setResolutionRemarks("Garbage cleared by morning collection truck and area disinfected.");
            complaintRepository.save(c2);

            ComplaintUpdate u4 = new ComplaintUpdate(c2, "PENDING", "Complaint submitted by citizen.", citizen);
            ComplaintUpdate u5 = new ComplaintUpdate(c2, "ASSIGNED", "Assigned to Sanitation Department", null);
            ComplaintUpdate u6 = new ComplaintUpdate(c2, "RESOLVED", "Garbage cleared by sanitation crew.", sanitationOfficer);
            updateRepository.saveAll(Arrays.asList(u4, u5, u6));

            Complaint c3 = new Complaint();
            c3.setComplaintId("CC1003");
            c3.setUser(citizen);
            c3.setCategory("OPEN_DRAIN");
            c3.setDescription("Uncovered stormwater drain right outside Government Primary School. Extreme danger for children!");
            c3.setLatitude(12.9650);
            c3.setLongitude(77.5850);
            c3.setPriority("CRITICAL");
            c3.setStatus("ASSIGNED");
            c3.setDepartment(pwdDept);
            complaintRepository.save(c3);

            ComplaintUpdate u7 = new ComplaintUpdate(c3, "PENDING", "Complaint submitted by citizen.", citizen);
            ComplaintUpdate u8 = new ComplaintUpdate(c3, "ASSIGNED", "Urgent assignment to Public Works Department.", null);
            updateRepository.saveAll(Arrays.asList(u7, u8));

            // Seed Initial Notifications
            notificationRepository.saveAll(Arrays.asList(
                    new Notification(citizen, c1, "Complaint #CC1001 is now In Progress by Road Department."),
                    new Notification(citizen, c2, "Complaint #CC1002 has been resolved. You can now view resolution proof."),
                    new Notification(citizen, c3, "Complaint #CC1003 assigned to Public Works Department.")
            ));
        }
    }
}
