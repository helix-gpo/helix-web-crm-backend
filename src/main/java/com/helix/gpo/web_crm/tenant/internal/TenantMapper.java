package com.helix.gpo.web_crm.tenant.internal;

import com.helix.gpo.web_crm.tenant.PartnerSummary;
import com.helix.gpo.web_crm.tenant.TenantBillingDetails;
import com.helix.gpo.web_crm.tenant.TenantSummary;
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

    static TenantBillingDetails toBillingDetails(Tenant tenant) {
        return new TenantBillingDetails(
                tenant.getId(),
                tenant.getCompanyName(),
                tenant.getLegalName(),
                tenant.getVatId(),
                tenant.getAddress(),
                tenant.getContactEmail()
        );
    }

    static PartnerSummary toPartnerSummary(Partner partner) {
        return new PartnerSummary(
                partner.getId(),
                partner.tenantId(),
                partner.getTenant().getCompanyName(),
                partner.getFirstName(),
                partner.getLastName(),
                partner.getRole()
        );
    }

    static TenantDtos.PartnerResponse toPartnerResponse(Partner partner) {
        return new TenantDtos.PartnerResponse(
                partner.getId(),
                partner.getFirstName(),
                partner.getLastName(),
                partner.getRole(),
                partner.getEmail(),
                partner.getPhone()
        );
    }

}
