package com.helix.gpo.web_crm.project.internal;

import com.helix.gpo.web_crm.project.MilestoneSummary;
import com.helix.gpo.web_crm.project.ProjectApi;
import com.helix.gpo.web_crm.project.ProjectSummary;
import com.helix.gpo.web_crm.project.PublicProjectSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class ProjectApiImpl implements ProjectApi {

    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;

    @Override
    public Optional<ProjectSummary> findSummaryById(UUID projectId) {
        return projectRepository.findById(projectId).map(ProjectMapper::toSummary);
    }

    @Override
    public List<ProjectSummary> findSummariesByTenant(UUID tenantId) {
        return projectRepository.findAllByTenantId(tenantId).stream()
                .map(ProjectMapper::toSummary)
                .toList();
    }

    @Override
    public Optional<MilestoneSummary> findMilestoneSummaryById(UUID milestoneId) {
        return milestoneRepository.findById(milestoneId).map(ProjectMapper::toMilestoneSummary);
    }

    @Override
    public List<MilestoneSummary> findMilestoneSummariesByProject(UUID projectId) {
        return milestoneRepository.findAllByProjectId(projectId).stream()
                .map(ProjectMapper::toMilestoneSummary)
                .toList();
    }

    // @Transactional nötig, weil toPublicSummary() auf tags/highlights
    // zugreift - beides @ElementCollection und damit lazy geladen. Ohne
    // offene Session hier: LazyInitializationException -> 500
    @Override
    @Transactional(readOnly = true)
    public List<PublicProjectSummary> findAllVisibleOnWebsite() {
        return projectRepository.findAllByVisibleOnWebsiteTrueOrderByCreatedAtDesc().stream()
                .map(ProjectMapper::toPublicSummary)
                .toList();
    }

}
