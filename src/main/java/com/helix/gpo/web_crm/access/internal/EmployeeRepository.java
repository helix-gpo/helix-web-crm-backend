package com.helix.gpo.web_crm.access.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByEmailIgnoreCase(String email);

    Optional<Employee> findByCognitoSub(String cognitoSub);

    boolean existsByEmailIgnoreCase(String email);

    @Query("select count(e) from Employee e where e.active = true and e.role.unrestricted = true")
    long countActiveAdmins();

    @Query("select count(e) from Employee e where e.active = true and e.role.unrestricted = true and e.role.id <> :roleId")
    long countActiveAdminsOutsideRole(@Param("roleId") UUID roleId);

    @Query("select count(e) from Employee e where e.active = true and e.role.id = :roleId")
    long countActiveInRole(@Param("roleId") UUID roleId);

    @Query("select count(e) > 0 from Employee e where e.role.id = :roleId")
    boolean existsAnyWithRole(@Param("roleId") UUID roleId);

}
