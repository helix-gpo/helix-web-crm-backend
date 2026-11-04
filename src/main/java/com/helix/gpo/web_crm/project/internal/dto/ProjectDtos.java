package com.helix.gpo.web_crm.project.internal.dto;

import com.helix.gpo.web_crm.project.internal.MilestoneStatus;
import com.helix.gpo.web_crm.project.internal.ProjectStatus;
import com.helix.gpo.web_crm.shared.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class ProjectDtos {

    private ProjectDtos() {
    }

    public record ProjectTagDto(
            @Size(max = 60) String value,
            @Size(max = 7) @Pattern(regexp = "^#[0-9a-fA-F]{6}$") String colorHex
    ) {}

    public record CreateProjectRequest(
            @NotNull UUID tenantId,
            @NotBlank @Size(max = 160) String title,
            @Size(max = 500) String description,
            @Size(max = 10000) String fullDescription,
            @Size(max = 10) List<@Size(max = 300) String> highlights,
            @Size(max = 10) @Valid List<ProjectTagDto> tags,
            LocalDate startDate,
            LocalDate endDate,
            ProjectStatus status
    ) {
    }

    public record ChangeStatusRequest(@NotNull ProjectStatus status) {
    }

    public record AddMilestoneRequest(
            @NotBlank @Size(max = 160) String title,
            @Size(max = 500) String description,
            LocalDate dueDate,
            Money price
    ) {
    }

    public record MilestoneResponse(
            UUID id,
            String title,
            String description,
            LocalDate dueDate,
            String status,
            Money price
    ) {
    }

    public record ProjectResponse(
            UUID id,
            UUID tenantId,
            String title,
            String description,
            String fullDescription,
            List<String> highlights,
            List<ProjectTagDto> tags,
            ProjectStatus status,
            LocalDate startDate,
            LocalDate endDate,
            boolean visibleOnWebsite,
            String notes,
            List<MilestoneResponse> milestones,
            String createdBy,
            Instant createdAt,
            String updatedBy,
            Instant updatedAt
    ) {
    }

    public record UpdateProjectRequest(
            @NotBlank @Size(max = 160) String title,
            @Size(max = 500) String description,
            @Size(max = 10000) String fullDescription,
            @Size(max = 10) List<@Size(max = 300) String> highlights,
            @Size(max = 10) @Valid List<ProjectTagDto> tags,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

    public record UpdateProjectNotesRequest(
            @Size(max = 10000) String notes
    ) {
    }

    public record UpdateMilestoneRequest(
            @NotBlank @Size(max = 160) String title,
            @Size(max = 500) String description,
            LocalDate dueDate,
            Money price,
            @NotNull MilestoneStatus status
    ) {
    }

    public record ChangeMilestoneStatusRequest(@NotNull MilestoneStatus status) {
    }

}
