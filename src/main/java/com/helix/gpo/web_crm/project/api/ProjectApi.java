package com.helix.gpo.web_crm.project.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectApi {

    Optional<ProjectSummary> findSummaryById(UUID projectId);

    List<ProjectSummary> findSummariesByTenant(UUID tenantId);

    Optional<MilestoneSummary> findMilestoneSummaryById(UUID milestoneId);

    List<MilestoneSummary> findMilestoneSummariesByProject(UUID projectId);

}
