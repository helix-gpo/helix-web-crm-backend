package com.helix.gpo.web_crm.project.internal.dto;

import com.helix.gpo.web_crm.project.internal.ProjectStatus;
import com.helix.gpo.web_crm.shared.Money;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class ProjectDtos {

    private ProjectDtos() {
    }

    public record ProjectTagDto(String value, String colorHex) {
    }

    public record CreateProjectRequest(
            @NotNull UUID tenantId,
            @NotBlank String title,
            String description,
            String fullDescription,
            List<String> highlights,
            List<ProjectTagDto> tags,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

    public record ChangeStatusRequest(@NotNull ProjectStatus status) {
    }

    public record AddMilestoneRequest(
            @NotBlank String title,
            String description,
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
            List<MilestoneResponse> milestones,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

}
