package com.helix.gpo.web_crm.tenant;

import java.util.UUID;

public record PartnerSummary(
        UUID id,
        UUID tenantId,
        String companyName,
        String firstName,
        String lastName,
        String role
) {
}
