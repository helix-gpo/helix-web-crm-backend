package com.helix.gpo.web_crm.access.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface EmployeeProjectAssignmentRepository extends JpaRepository<EmployeeProjectAssignment, UUID> {

    List<EmployeeProjectAssignment> findAllByEmployeeId(UUID employeeId);

    void deleteByEmployeeIdAndProjectId(UUID employeeId, UUID projectId);

    boolean existsByEmployeeIdAndProjectId(UUID employeeId, UUID projectId);

}

