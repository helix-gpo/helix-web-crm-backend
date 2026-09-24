package com.helix.gpo.web_crm.access.internal.dto;

import com.helix.gpo.web_crm.access.EntityType;
import com.helix.gpo.web_crm.access.PermissionAction;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public final class AccessDtos {

    private AccessDtos() {
    }

    // create
    public record PermissionEntryDto(
            @NotNull EntityType entity,
            @NotNull PermissionAction action
    ) {
    }

    public record CreateRoleRequest(
            @NotBlank String name,
            String description,
            Boolean unrestricted,
            List<PermissionEntryDto> permissions
    ) {
    }

    public record CreateEmployeeRequest(
            @NotBlank @Email String email,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotNull UUID roleId
    ) {
    }

    // update
    public record UpdateRoleRequest(
            @NotBlank String name,
            String description,
            Boolean unrestricted,
            List<PermissionEntryDto> permissions
    ) {
    }

    public record UpdateEmployeeRoleRequest(
            @NotNull UUID roleId
    ) {
    }

    // responses
    public record RoleResponse(
            UUID id,
            String name,
            String description,
            boolean unrestricted,
            List<PermissionEntryDto> permissions
    ) {
    }

    public record EmployeeResponse(
            UUID id,
            String email,
            String firstName,
            String lastName,
            RoleResponse role,
            boolean active,
            List<UUID> assignedProjectIds
    ) {
    }

}
