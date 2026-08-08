package com.helix.gpo.web_crm.tenant.internal;

import com.helix.gpo.web_crm.tenant.api.TenantSummary;
import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos;

final class TenantMapper {

    private TenantMapper() {
    }

    static TenantDtos.TenantResponse toResponse(Tenant tenant) {
        return new TenantDtos.TenantResponse(
                tenant.getId(),
                tenant.getCompanyName(),
                tenant.getLegalName(),
                tenant.getVatId(),
                tenant.getAddress(),
                tenant.getContactEmail(),
                tenant.getContactPhone(),
                tenant.getStatus(),
                tenant.isVisibleOnWebsite(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }

    static TenantSummary toSummary(Tenant tenant) {
        return new TenantSummary(
                tenant.getId(),
                tenant.getCompanyName(),
                tenant.getStatus().name()
        );
    }

}
