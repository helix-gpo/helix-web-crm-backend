package com.helix.gpo.web_crm.project.internal;

import com.helix.gpo.web_crm.project.MilestoneSummary;
import com.helix.gpo.web_crm.project.ProjectSummary;
import com.helix.gpo.web_crm.project.PublicProjectSummary;
import com.helix.gpo.web_crm.project.internal.dto.ProjectDtos.MilestoneResponse;
import com.helix.gpo.web_crm.project.internal.dto.ProjectDtos.ProjectResponse;
import com.helix.gpo.web_crm.project.internal.dto.ProjectDtos.ProjectTagDto;

import java.util.List;

final class ProjectMapper {

    private ProjectMapper() {
    }

    static ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getTenantId(),
                project.getTitle(),
                project.getDescription(),
                project.getFullDescription(),
                List.copyOf(project.getHighlights()),
                project.getTags().stream()
                        .map(tag -> new ProjectTagDto(tag.value(), tag.colorHex()))
                        .toList(),
                project.getStatus(),
                project.getStartDate(),
                project.getEndDate(),
                project.isVisibleOnWebsite(),
                project.getMilestones().stream()
                        .map(ProjectMapper::toMilestoneResponse)
                        .toList(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    static MilestoneResponse toMilestoneResponse(Milestone milestone) {
        return new MilestoneResponse(
                milestone.getId(),
                milestone.getTitle(),
                milestone.getDescription(),
                milestone.getDueDate(),
                milestone.getStatus().name(),
                milestone.getPrice()
        );
    }

    static ProjectSummary toSummary(Project project) {
        return new ProjectSummary(
                project.getId(),
                project.getTenantId(),
                project.getTitle(),
                project.getStatus().name()
        );
    }

    static MilestoneSummary toMilestoneSummary(Milestone milestone) {
        return new MilestoneSummary(
                milestone.getId(),
                milestone.getProject().getId(),
                milestone.getTitle(),
                milestone.getDueDate(),
                milestone.getStatus().name(),
                milestone.getPrice()
        );
    }

    static PublicProjectSummary toPublicSummary(Project project) {
        return new PublicProjectSummary(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getFullDescription(),
                List.copyOf(project.getHighlights()),
                project.getTags().stream()
                        .map(tag -> new PublicProjectSummary.ProjectTagView(
                                tag.value(), tag.colorHex()))
                        .toList(),
                project.getStartDate()
        );
    }

}
