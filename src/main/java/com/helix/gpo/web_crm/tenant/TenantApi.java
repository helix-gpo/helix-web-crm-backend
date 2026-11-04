package com.helix.gpo.web_crm.tenant;

import java.util.Optional;
import java.util.UUID;

public interface TenantApi {

    boolean existsAndIsActive(UUID tenantId);

    Optional<TenantBillingDetails> findBillingDetailsById(UUID tenantId);

    Optional<PartnerSummary> findPartnerSummaryById(UUID partnerId);

    Optional<String> findCreatedBy(UUID tenantId);

}
