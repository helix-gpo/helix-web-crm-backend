package com.helix.gpo.web_crm.project.internal;

import com.helix.gpo.web_crm.project.internal.dto.ProjectDtos.*;
import com.helix.gpo.web_crm.tenant.api.TenantApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
class ProjectService {

    private final ProjectRepository projectRepository;
    private final TenantApi tenantApi;

    ProjectResponse create(CreateProjectRequest request) {
        if (!tenantApi.existsAndIsActive(request.tenantId())) {
            throw new IllegalStateException("Tenant is not active or does not exist: " + request.tenantId());
        }

        Project project = Project.builder()
                .tenantId(request.tenantId())
                .title(request.title())
                .description(request.description())
                .fullDescription(request.fullDescription())
                .highlights(request.highlights() != null ? request.highlights() : List.of())
                .tags(request.tags() != null
                        ? request.tags().stream().map(t -> new ProjectTag(t.value(), t.colorHex())).toList()
                        : List.of())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        return ProjectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    ProjectResponse findById(UUID id) {
        return ProjectMapper.toResponse(getProjectOrThrow(id));
    }

    @Transactional(readOnly = true)
    List<ProjectResponse> findAllByTenant(UUID tenantId) {
        return projectRepository.findAllByTenantId(tenantId).stream()
                .map(ProjectMapper::toResponse)
                .toList();
    }

    ProjectResponse changeStatus(UUID id, ChangeStatusRequest request) {
        Project project = getProjectOrThrow(id);
        project.changeStatus(request.status());
        return ProjectMapper.toResponse(project);
    }

    ProjectResponse publishOnWebsite(UUID id) {
        Project project = getProjectOrThrow(id);
        project.publishOnWebsite();
        return ProjectMapper.toResponse(project);
    }

    ProjectResponse unpublishFromWebsite(UUID id) {
        Project project = getProjectOrThrow(id);
        project.unpublishFromWebsite();
        return ProjectMapper.toResponse(project);
    }

    MilestoneResponse addMilestone(UUID projectId, AddMilestoneRequest request) {
        Project project = getProjectOrThrow(projectId);
        Milestone milestone = project.addMilestone(
                request.title(),
                request.description(),
                request.dueDate(),
                request.price()
        );
        projectRepository.save(project);
        return ProjectMapper.toMilestoneResponse(milestone);
    }

    private Project getProjectOrThrow(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + id));
    }

}
