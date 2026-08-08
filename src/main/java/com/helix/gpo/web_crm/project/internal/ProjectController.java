package com.helix.gpo.web_crm.project.internal;

import com.helix.gpo.web_crm.project.internal.dto.ProjectDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/crm/projects")
@RequiredArgsConstructor
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

    @GetMapping
    List<ProjectResponse> findAllByTenant(@RequestParam UUID tenantId) {
        return projectService.findAllByTenant(tenantId);
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

    @PostMapping("/{id}/milestones")
    MilestoneResponse addMilestone(@PathVariable UUID id, @Valid @RequestBody AddMilestoneRequest request) {
        return projectService.addMilestone(id, request);
    }

}
