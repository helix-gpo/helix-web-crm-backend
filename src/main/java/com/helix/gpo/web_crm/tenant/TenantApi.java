package com.helix.gpo.web_crm.tenant;

import java.util.Optional;
import java.util.UUID;

public interface TenantApi {

    Optional<TenantSummary> findSummaryById(UUID tenantId);

    boolean existsAndIsActive(UUID tenantId);

    Optional<TenantBillingDetails> findBillingDetailsById(UUID tenantId);

    Optional<PartnerSummary> findPartnerSummaryById(UUID partnerId);

}
