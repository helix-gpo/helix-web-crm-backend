package com.helix.gpo.web_crm.project.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findAllByTenantId(UUID tenantId);

    long countByVisibleOnWebsiteTrue();

    List<Project> findAllByVisibleOnWebsiteTrueOrderByCreatedAtDesc();

}
