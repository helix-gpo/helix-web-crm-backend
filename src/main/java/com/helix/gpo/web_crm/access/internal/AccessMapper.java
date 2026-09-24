package com.helix.gpo.web_crm.access.internal;

import com.helix.gpo.web_crm.access.internal.dto.AccessDtos.EmployeeResponse;
import com.helix.gpo.web_crm.access.internal.dto.AccessDtos.PermissionEntryDto;
import com.helix.gpo.web_crm.access.internal.dto.AccessDtos.RoleResponse;

import java.util.List;
import java.util.Set;

final class AccessMapper {

    private AccessMapper() {
    }

    static RoleResponse toResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.isUnrestricted(),
                role.getPermissions().stream()
                        .map(p -> new PermissionEntryDto(p.entityType(), p.action()))
                        .toList()
        );
    }

    static EmployeeResponse toResponse(Employee employee, List<java.util.UUID> assignedProjectIds) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmail(),
                employee.getFirstName(),
                employee.getLastName(),
                toResponse(employee.getRole()),
                employee.isActive(),
                assignedProjectIds
        );
    }

    static Set<RolePermission> toPermissionSet(List<PermissionEntryDto> dtos) {
        if (dtos == null) {
            return Set.of();
        }
        return dtos.stream()
                .map(dto -> new RolePermission(dto.entity(), dto.action()))
                .collect(java.util.stream.Collectors.toSet());
    }

}
