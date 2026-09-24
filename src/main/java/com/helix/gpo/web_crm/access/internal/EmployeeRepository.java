package com.helix.gpo.web_crm.access.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

}
