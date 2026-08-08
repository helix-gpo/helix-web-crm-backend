package com.helix.gpo.web_crm.tenant.api;

import java.util.UUID;

public record TenantSummary(
        UUID id,
        String companyName,
        String status
) {
}
