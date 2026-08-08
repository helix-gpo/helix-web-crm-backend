package com.helix.gpo.web_crm.tenant.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface TenantRepository extends JpaRepository<Tenant, UUID> {

    List<Tenant> findAllByVisibleOnWebsiteTrue();

}
