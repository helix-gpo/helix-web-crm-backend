package com.helix.gpo.web_crm.tenant.internal;

import com.helix.gpo.web_crm.tenant.PartnerSummary;
import com.helix.gpo.web_crm.tenant.TenantBillingDetails;
import com.helix.gpo.web_crm.tenant.TenantSummary;
import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos;

final class TenantMapper {

    private TenantMapper() {
    }

    static TenantDtos.TenantResponse toResponse(Tenant tenant, String logoUrl) {
        return new TenantDtos.TenantResponse(
                tenant.getId(),
                tenant.getCompanyName(),
                tenant.getLegalName(),
                tenant.getVatId(),
                tenant.getReferenceCode(),
                tenant.getAddress(),
                tenant.getContactEmail(),
                tenant.getContactPhone(),
                tenant.getWebsiteUrl(),
                tenant.getNotes(),
                tenant.getStatus(),
                tenant.isVisibleOnWebsite(),
                logoUrl,
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
                tenant.getReferenceCode(),
                tenant.getAddress(),
                tenant.getContactEmail()
        );
    }

    static TenantDtos.PartnerResponse toPartnerResponse(Partner partner, String photoUrl) {
        return new TenantDtos.PartnerResponse(
                partner.getId(),
                partner.getFirstName(),
                partner.getLastName(),
                partner.getRole(),
                partner.getEmail(),
                partner.getPhone(),
                photoUrl
        );
    }

    static PartnerSummary toPartnerSummary(Partner partner, String photoUrl) {
        return new PartnerSummary(
                partner.getId(),
                partner.tenantId(),
                partner.getTenant().getCompanyName(),
                partner.getFirstName(),
                partner.getLastName(),
                partner.getRole(),
                partner.getEmail(),
                photoUrl
        );
    }

}
