package com.helix.gpo.web_crm.project.internal;

import com.helix.gpo.web_crm.project.internal.dto.ProjectDtos.*;
import com.helix.gpo.web_crm.tenant.TenantApi;
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
    private final MilestoneRepository milestoneRepository;
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
    List<ProjectResponse> findAll() {
        return projectRepository.findAll().stream()
                .map(ProjectMapper::toResponse)
                .toList();
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

    ProjectResponse update(UUID id, UpdateProjectRequest request) {
        Project project = getProjectOrThrow(id);
        project.updateDetails(
                request.title(),
                request.description(),
                request.fullDescription(),
                request.highlights(),
                request.tags() != null
                        ? request.tags().stream().map(t -> new ProjectTag(t.value(), t.colorHex())).toList()
                        : List.of(),
                request.startDate(),
                request.endDate()
        );
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

    MilestoneResponse updateMilestone(UUID milestoneId, UpdateMilestoneRequest request) {
        Milestone milestone = getMilestoneOrThrow(milestoneId);
        milestone.updateDetails(request.title(), request.description(), request.dueDate(), request.price(), request.status());
        return ProjectMapper.toMilestoneResponse(milestone);
    }

    MilestoneResponse changeMilestoneStatus(UUID milestoneId, ChangeMilestoneStatusRequest request) {
        Milestone milestone = getMilestoneOrThrow(milestoneId);
        milestone.changeStatus(request.status());
        return ProjectMapper.toMilestoneResponse(milestone);
    }

    void removeMilestone(UUID milestoneId) {
        if (!milestoneRepository.existsById(milestoneId)) {
            throw new EntityNotFoundException("Milestone not found: " + milestoneId);
        }
        milestoneRepository.deleteById(milestoneId);
    }

    private Milestone getMilestoneOrThrow(UUID id) {
        return milestoneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Milestone not found: " + id));
    }

}
