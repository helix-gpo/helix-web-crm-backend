package com.helix.gpo.web_crm.project.internal;

import com.helix.gpo.web_crm.access.AccessApi;
import com.helix.gpo.web_crm.access.EntityType;
import com.helix.gpo.web_crm.project.internal.dto.ProjectDtos;
import com.helix.gpo.web_crm.shared.ImageUploadValidator;
import com.helix.gpo.web_crm.storage.StorageApi;
import com.helix.gpo.web_crm.tenant.TenantApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class ProjectService {

    private static final int MAX_VISIBLE_ON_WEBSITE = 6;

    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;
    private final TenantApi tenantApi;
    private final StorageApi storageApi;
    private final AccessApi accessApi;

    ProjectDtos.ProjectResponse create(ProjectDtos.CreateProjectRequest request) {
        accessApi.requireWrite(EntityType.PROJECT);

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
                .status(request.status() != null ? request.status() : ProjectStatus.LEAD)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        return toResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    ProjectDtos.ProjectResponse findById(UUID id) {
        accessApi.requireRead(EntityType.PROJECT);
        Project project = getProjectOrThrow(id);
        requireProjectAccess(project.getId());
        return toResponse(project);
    }

    @Transactional(readOnly = true)
    List<ProjectDtos.ProjectResponse> findAll() {
        accessApi.requireRead(EntityType.PROJECT);
        return filterAccessible(projectRepository.findAll()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    List<ProjectDtos.ProjectResponse> findAllByTenant(UUID tenantId) {
        accessApi.requireRead(EntityType.PROJECT);
        return filterAccessible(projectRepository.findAllByTenantId(tenantId)).stream()
                .map(this::toResponse)
                .toList();
    }

    ProjectDtos.ProjectResponse changeStatus(UUID id, ProjectDtos.ChangeStatusRequest request) {
        accessApi.requireWrite(EntityType.PROJECT);
        Project project = getProjectOrThrow(id);
        requireProjectAccess(project.getId());

        project.changeStatus(request.status());
        return toResponse(project);
    }

    ProjectDtos.ProjectResponse publishOnWebsite(UUID id) {
        accessApi.requireWrite(EntityType.PROJECT);
        Project project = getProjectOrThrow(id);
        requireProjectAccess(project.getId());

        if (!project.isVisibleOnWebsite() && projectRepository.countByVisibleOnWebsiteTrue() >= MAX_VISIBLE_ON_WEBSITE) {
            throw new IllegalStateException(
                    "Es können maximal " + MAX_VISIBLE_ON_WEBSITE + " Projekte gleichzeitig auf der Website sichtbar sein");
        }

        project.publishOnWebsite();
        return toResponse(project);
    }

    ProjectDtos.ProjectResponse unpublishFromWebsite(UUID id) {
        accessApi.requireWrite(EntityType.PROJECT);
        Project project = getProjectOrThrow(id);
        requireProjectAccess(project.getId());

        project.unpublishFromWebsite();
        return toResponse(project);
    }

    ProjectDtos.ProjectResponse update(UUID id, ProjectDtos.UpdateProjectRequest request) {
        accessApi.requireWrite(EntityType.PROJECT);
        Project project = getProjectOrThrow(id);
        requireProjectAccess(project.getId());

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
        return toResponse(project);
    }

    ProjectDtos.ProjectResponse uploadImage(UUID projectId, MultipartFile file) {
        accessApi.requireWrite(EntityType.PROJECT);
        Project project = getProjectOrThrow(projectId);
        requireProjectAccess(project.getId());

        ImageUploadValidator.validate(file);

        if (project.getImageKey() != null) {
            storageApi.delete(project.getImageKey());
        }

        String key = ImageUploadValidator.generateKey("project-images", projectId, file);
        try {
            storageApi.upload(key, file.getBytes(), file.getContentType());
        } catch (IOException e) {
            throw new IllegalStateException("Bild konnte nicht hochgeladen werden.", e);
        }

        project.attachImage(key);
        return toResponse(project);
    }

    ProjectDtos.ProjectResponse removeImage(UUID projectId) {
        accessApi.requireWrite(EntityType.PROJECT);
        Project project = getProjectOrThrow(projectId);
        requireProjectAccess(project.getId());

        if (project.getImageKey() != null) {
            storageApi.delete(project.getImageKey());
            project.removeImage();
        }
        return toResponse(project);
    }

    ProjectDtos.ProjectResponse updateNotes(UUID id, ProjectDtos.UpdateProjectNotesRequest request) {
        accessApi.requireWrite(EntityType.PROJECT);
        Project project = getProjectOrThrow(id);
        requireProjectAccess(project.getId());

        project.updateNotes(request.notes());
        return toResponse(project);
    }

    ProjectDtos.MilestoneResponse addMilestone(UUID projectId, ProjectDtos.AddMilestoneRequest request) {
        accessApi.requireWrite(EntityType.MILESTONE);
        Project project = getProjectOrThrow(projectId);
        requireProjectAccess(project.getId());

        Milestone milestone = project.addMilestone(
                request.title(),
                request.description(),
                request.dueDate(),
                request.price()
        );
        projectRepository.save(project);
        return ProjectMapper.toMilestoneResponse(milestone);
    }

    ProjectDtos.MilestoneResponse updateMilestone(UUID milestoneId, ProjectDtos.UpdateMilestoneRequest request) {
        accessApi.requireWrite(EntityType.MILESTONE);
        Milestone milestone = getMilestoneOrThrow(milestoneId);
        requireProjectAccess(milestone.getProject().getId());

        milestone.updateDetails(request.title(), request.description(), request.dueDate(), request.price(), request.status());
        return ProjectMapper.toMilestoneResponse(milestone);
    }

    ProjectDtos.MilestoneResponse changeMilestoneStatus(UUID milestoneId, ProjectDtos.ChangeMilestoneStatusRequest request) {
        accessApi.requireWrite(EntityType.MILESTONE);
        Milestone milestone = getMilestoneOrThrow(milestoneId);
        requireProjectAccess(milestone.getProject().getId());

        milestone.changeStatus(request.status());
        return ProjectMapper.toMilestoneResponse(milestone);
    }

    void removeMilestone(UUID milestoneId) {
        accessApi.requireDelete(EntityType.MILESTONE);
        Milestone milestone = getMilestoneOrThrow(milestoneId);
        requireProjectAccess(milestone.getProject().getId());

        milestoneRepository.delete(milestone);
    }

    private List<Project> filterAccessible(List<Project> projects) {
        if (accessApi.isUnrestricted()) {
            return projects;
        }
        List<UUID> accessibleIds = accessApi.accessibleProjectIds();
        return projects.stream().filter(p -> accessibleIds.contains(p.getId())).toList();
    }

    private void requireProjectAccess(UUID projectId) {
        if (!accessApi.canAccessProject(projectId)) {
            throw new AccessDeniedException("Kein Zugriff auf dieses Projekt.");
        }
    }

    private Project getProjectOrThrow(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dieses Projekt wurde nicht gefunden."));
    }

    private Milestone getMilestoneOrThrow(UUID id) {
        return milestoneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dieser Meilenstein wurde nicht gefunden."));
    }

    private ProjectDtos.ProjectResponse toResponse(Project project) {
        String imageUrl = project.getImageKey() != null
                ? storageApi.presignedUrl(project.getImageKey(), Duration.ofMinutes(30))
                : null;
        return ProjectMapper.toResponse(project, imageUrl);
    }

}
