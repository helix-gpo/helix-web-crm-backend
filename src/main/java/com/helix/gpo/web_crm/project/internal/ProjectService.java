package com.helix.gpo.web_crm.project.internal;

import com.helix.gpo.web_crm.project.internal.dto.ProjectDtos.*;
import com.helix.gpo.web_crm.shared.ImageUploadValidator;
import com.helix.gpo.web_crm.storage.StorageApi;
import com.helix.gpo.web_crm.tenant.TenantApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
class ProjectService {

    private static final int MAX_VISIBLE_ON_WEBSITE = 6;

    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;
    private final TenantApi tenantApi;
    private final StorageApi storageApi;

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
                .status(request.status() != null ? request.status() : ProjectStatus.LEAD)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        return toResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    ProjectResponse findById(UUID id) {
        return toResponse(getProjectOrThrow(id));
    }

    @Transactional(readOnly = true)
    List<ProjectResponse> findAll() {
        return projectRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    List<ProjectResponse> findAllByTenant(UUID tenantId) {
        return projectRepository.findAllByTenantId(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    ProjectResponse changeStatus(UUID id, ChangeStatusRequest request) {
        Project project = getProjectOrThrow(id);
        project.changeStatus(request.status());
        return toResponse(project);
    }

    ProjectResponse publishOnWebsite(UUID id) {
        Project project = getProjectOrThrow(id);

        if (!project.isVisibleOnWebsite() && projectRepository.countByVisibleOnWebsiteTrue() >= MAX_VISIBLE_ON_WEBSITE) {
            throw new IllegalStateException(
                    "Es können maximal " + MAX_VISIBLE_ON_WEBSITE + " Projekte gleichzeitig auf der Website sichtbar sein");
        }

        project.publishOnWebsite();
        return toResponse(project);
    }

    ProjectResponse unpublishFromWebsite(UUID id) {
        Project project = getProjectOrThrow(id);
        project.unpublishFromWebsite();
        return toResponse(project);
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
        return toResponse(project);
    }

    ProjectResponse uploadImage(UUID projectId, MultipartFile file) {
        Project project = getProjectOrThrow(projectId);
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

    ProjectResponse removeImage(UUID projectId) {
        Project project = getProjectOrThrow(projectId);
        if (project.getImageKey() != null) {
            storageApi.delete(project.getImageKey());
            project.removeImage();
        }
        return toResponse(project);
    }

    ProjectResponse updateNotes(UUID id, UpdateProjectNotesRequest request) {
        Project project = getProjectOrThrow(id);
        project.updateNotes(request.notes());
        return toResponse(project);
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

    private ProjectResponse toResponse(Project project) {
        String imageUrl = project.getImageKey() != null
                ? storageApi.presignedUrl(project.getImageKey(), Duration.ofMinutes(30))
                : null;
        return ProjectMapper.toResponse(project, imageUrl);
    }

}
