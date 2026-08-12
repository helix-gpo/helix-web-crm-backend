package com.helix.gpo.web_crm.tenant;

import com.helix.gpo.web_crm.shared.Address;

import java.util.UUID;

public record TenantBillingDetails(
        UUID tenantId,
        String companyName,
        String legalName,
        String vatId,
        String referenceCode,
        Address address,
        String contactEmail
) {
}
