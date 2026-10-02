package com.helix.gpo.web_crm.access.internal;

import com.helix.gpo.web_crm.access.AccessApi;
import com.helix.gpo.web_crm.access.EntityType;
import com.helix.gpo.web_crm.access.PermissionAction;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AccessApiImpl implements AccessApi {

    private final EmployeeRepository employeeRepository;
    private final EmployeeProjectAssignmentRepository assignmentRepository;

    @Override
    public boolean isUnrestricted() {
        return currentEmployee().map(e -> e.getRole().isUnrestricted()).orElse(true);
    }

    @Override
    public boolean canRead(EntityType entity) {
        return isAllowed(entity, PermissionAction.READ);
    }

    @Override
    public boolean canWrite(EntityType entity) {
        return isAllowed(entity, PermissionAction.WRITE);
    }

    @Override
    public boolean canDelete(EntityType entity) {
        return isAllowed(entity, PermissionAction.DELETE);
    }

    @Override
    public void requireRead(EntityType entity) {
        require(entity, PermissionAction.READ);
    }

    @Override
    public void requireWrite(EntityType entity) {
        require(entity, PermissionAction.WRITE);
    }

    @Override
    public void requireDelete(EntityType entity) {
        require(entity, PermissionAction.DELETE);
    }

    @Override
    public List<UUID> accessibleProjectIds() {
        return currentEmployee()
                .map(e -> assignmentRepository.findAllByEmployeeId(e.getId()).stream()
                        .map(EmployeeProjectAssignment::getProjectId)
                        .toList())
                .orElse(List.of());
    }

    @Override
    public List<UUID> accessibleTenantIds() {
        return currentEmployee()
                .map(e -> assignmentRepository.findAllByEmployeeId(e.getId()).stream()
                        .map(EmployeeProjectAssignment::getTenantId)
                        .distinct()
                        .toList())
                .orElse(List.of());
    }

    @Override
    public boolean canAccessProject(UUID projectId) {
        return isUnrestricted() || accessibleProjectIds().contains(projectId);
    }

    @Override
    public boolean canAccessTenant(UUID tenantId) {
        return isUnrestricted() || accessibleTenantIds().contains(tenantId);
    }

    private boolean isAllowed(EntityType entity, PermissionAction action) {
        // no employee record (e.g. legacy accounts not yet migrated) = full access,
        // keeps existing admins usable right after this migration ships
        return currentEmployee().map(e -> e.getRole().allows(entity, action)).orElse(true);
    }

    private void require(EntityType entity, PermissionAction action) {
        if (!isAllowed(entity, action)) {
            throw new AccessDeniedException(
                    "Keine Berechtigung für " + action + " auf " + entity + ".");
        }
    }

    @Override
    public String currentUserEmail() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("email");
        }
        return null;
    }

    private Optional<Employee> currentEmployee() {
        String email = currentUserEmail();
        if (email == null) {
            return Optional.empty();
        }
        return employeeRepository.findByEmailIgnoreCase(email);
    }

}
