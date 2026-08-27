package com.helix.gpo.web_crm.project.internal;

import com.helix.gpo.web_crm.project.internal.dto.ProjectDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crm/projects")
class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    ResponseEntity<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.create(request);
        return ResponseEntity.created(URI.create("/api/crm/projects/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    ProjectResponse findById(@PathVariable UUID id) {
        return projectService.findById(id);
    }

    // Ersetzt die alte findAllByTenant(@RequestParam UUID tenantId)
    @GetMapping
    List<ProjectResponse> findAll(@RequestParam(required = false) UUID tenantId) {
        return tenantId != null ? projectService.findAllByTenant(tenantId) : projectService.findAll();
    }

    @PatchMapping("/{id}/status")
    ProjectResponse changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest request) {
        return projectService.changeStatus(id, request);
    }

    @PostMapping("/{id}/publish")
    ProjectResponse publishOnWebsite(@PathVariable UUID id) {
        return projectService.publishOnWebsite(id);
    }

    @PostMapping("/{id}/unpublish")
    ProjectResponse unpublishFromWebsite(@PathVariable UUID id) {
        return projectService.unpublishFromWebsite(id);
    }

    @PatchMapping("/{id}")
    ProjectResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateProjectRequest request) {
        return projectService.update(id, request);
    }

    @PostMapping("/{id}/milestones")
    MilestoneResponse addMilestone(@PathVariable UUID id, @Valid @RequestBody AddMilestoneRequest request) {
        return projectService.addMilestone(id, request);
    }

    @PatchMapping("/{projectId}/milestones/{milestoneId}")
    MilestoneResponse updateMilestone(@PathVariable UUID projectId, @PathVariable UUID milestoneId,
                                      @Valid @RequestBody UpdateMilestoneRequest request) {
        return projectService.updateMilestone(milestoneId, request);
    }

    @PatchMapping("/{projectId}/milestones/{milestoneId}/status")
    MilestoneResponse changeMilestoneStatus(@PathVariable UUID projectId, @PathVariable UUID milestoneId,
                                            @Valid @RequestBody ChangeMilestoneStatusRequest request) {
        return projectService.changeMilestoneStatus(milestoneId, request);
    }

    @DeleteMapping("/{projectId}/milestones/{milestoneId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removeMilestone(@PathVariable UUID projectId, @PathVariable UUID milestoneId) {
        projectService.removeMilestone(milestoneId);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ProjectResponse uploadImage(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return projectService.uploadImage(id, file);
    }

    @DeleteMapping("/{id}/image")
    ProjectResponse removeImage(@PathVariable UUID id) {
        return projectService.removeImage(id);
    }

    @PatchMapping("/{id}/notes")
    ProjectResponse updateNotes(@PathVariable UUID id, @RequestBody UpdateProjectNotesRequest request) {
        return projectService.updateNotes(id, request);
    }

}
