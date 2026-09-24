package com.helix.gpo.web_crm.project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectApi {

    Optional<ProjectSummary> findSummaryById(UUID projectId);

    Optional<MilestoneSummary> findMilestoneSummaryById(UUID milestoneId);

    List<MilestoneSummary> findMilestoneSummariesByProject(UUID projectId);

    List<PublicProjectSummary> findAllVisibleOnWebsite();

}
