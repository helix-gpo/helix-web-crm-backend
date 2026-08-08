package com.helix.gpo.web_crm.tenant.api;

import java.util.Optional;
import java.util.UUID;

public interface TenantApi {

    Optional<TenantSummary> findSummaryById(UUID tenantId);

    boolean existsAndIsActive(UUID tenantId);

}
