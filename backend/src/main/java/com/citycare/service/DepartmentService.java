package com.citycare.service;

import com.citycare.entity.Department;
import com.citycare.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Optional<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }

    /**
     * Automatic Department Assignment (Phase 8):
     * Pothole / Road Damage -> Road Department
     * Garbage -> Sanitation Department
     * Water Leakage -> Water Department
     * Broken Streetlight -> Electricity Department
     * Open Drain / Manhole -> Public Works Department
     * Other -> General Civic Department
     */
    public Department routeCategoryToDepartment(String category) {
        String deptCode;
        switch (category.toUpperCase()) {
            case "POTHOLE":
            case "ROAD_DAMAGE":
                deptCode = "ROAD";
                break;
            case "GARBAGE":
                deptCode = "SANITATION";
                break;
            case "WATER_LEAKAGE":
                deptCode = "WATER";
                break;
            case "BROKEN_STREETLIGHT":
                deptCode = "ELECTRICITY";
                break;
            case "OPEN_DRAIN":
                deptCode = "PWD";
                break;
            default:
                deptCode = "CIVIC";
                break;
        }

        return departmentRepository.findByCode(deptCode)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(null));
    }
}
