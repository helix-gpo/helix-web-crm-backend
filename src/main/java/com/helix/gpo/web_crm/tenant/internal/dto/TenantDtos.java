package com.helix.gpo.web_crm.tenant.internal.dto;

import com.helix.gpo.web_crm.shared.Address;
import com.helix.gpo.web_crm.tenant.internal.TenantStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class TenantDtos {

    private TenantDtos() {
    }

    public record CreateTenantRequest(
            @NotBlank @Size(max = 160) String companyName,
            @Size(max = 160) String legalName,
            @Size(max = 20) String vatId,
            @NotBlank @Size(max = 12) String referenceCode,
            @Valid Address address,
            @Email @Size(max = 254) String contactEmail,
            @Size(max = 30) String contactPhone,
            @Size(max = 500) String websiteUrl,
            List<@Valid CreatePartnerRequest> partners
    ) {
    }

    public record UpdateContactDetailsRequest(
            @Email @Size(max = 254) String contactEmail,
            @Size(max = 30) String contactPhone,
            @Valid Address address,
            @Size(max = 500) String websiteUrl
    ) {
    }

    public record UpdateCoreDetailsRequest(
            @NotBlank @Size(max = 160) String companyName,
            @Size(max = 160) String legalName,
            @Size(max = 20) String vatId,
            @NotBlank @Size(max = 12) String referenceCode
    ) {
    }

    public record UpdateNotesRequest(
            @Size(max = 10000) String notes
    ) {
    }

    public record TenantResponse(
            UUID id,
            String companyName,
            String legalName,
            String vatId,
            String referenceCode,
            Address address,
            String contactEmail,
            String contactPhone,
            String websiteUrl,
            String notes,
            TenantStatus status,
            boolean visibleOnWebsite,
            String logoUrl,
            String createdBy,
            Instant createdAt,
            String updatedBy,
            Instant updatedAt
    ) {
    }

    public record PartnerResponse(
            UUID id,
            String firstName,
            String lastName,
            String role,
            String email,
            String phone
    ) {
    }

    public record CreatePartnerRequest(
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName,
            @Size(max = 100) String role,
            @Email @Size(max = 254) String email,
            @Size(max = 30) String phone
    ) {
    }

    public record UpdatePartnerRequest(
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName,
            @Size(max = 100) String role,
            @Email @Size(max = 254) String email,
            @Size(max = 30) String phone
    ) {
    }

}
