package com.helix.gpo.web_crm.access.internal.dto;

import com.helix.gpo.web_crm.access.EntityType;
import com.helix.gpo.web_crm.access.PermissionAction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
            @NotBlank @Size(max = 60) String name,
            @Size(max = 255) String description,
            Boolean unrestricted,
            @Valid List<PermissionEntryDto> permissions
    ) {
    }

    public record CreateEmployeeRequest(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName,
            @NotNull UUID roleId
    ) {
    }

    // update
    public record UpdateRoleRequest(
            @NotBlank @Size(max = 60) String name,
            @Size(max = 255) String description,
            Boolean unrestricted,
            @Valid List<PermissionEntryDto> permissions
    ) {
    }

    public record UpdateEmployeeRequest(
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName,
            @NotNull UUID roleId
    ) {
    }

    public record AssignProjectRequest(
            @NotNull UUID tenantId
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

    public record MeResponse(
            String email,
            String firstName,
            String lastName,
            String roleName,
            boolean unrestricted,
            List<PermissionEntryDto> permissions
    ) {
    }

}
