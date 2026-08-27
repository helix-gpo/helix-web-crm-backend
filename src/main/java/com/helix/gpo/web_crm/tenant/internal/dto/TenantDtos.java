package com.helix.gpo.web_crm.tenant.internal.dto;

import com.helix.gpo.web_crm.shared.Address;
import com.helix.gpo.web_crm.tenant.internal.TenantStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

public final class TenantDtos {

    private TenantDtos() {
    }

    public record CreateTenantRequest(
            @NotBlank String companyName,
            String legalName,
            String vatId,
            String referenceCode,
            Address address,
            @Email String contactEmail,
            String contactPhone,
            String websiteUrl
    ) {
    }

    public record UpdateContactDetailsRequest(
            @Email String contactEmail,
            String contactPhone,
            Address address,
            String websiteUrl
    ) {
    }

    public record UpdateCoreDetailsRequest(
            @NotBlank String companyName,
            String legalName,
            String vatId,
            String referenceCode
    ) {
    }

    public record UpdateNotesRequest(
            String notes
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
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record PartnerResponse(
            UUID id,
            String firstName,
            String lastName,
            String role,
            String email,
            String phone,
            String photoUrl
    ) {
    }

    public record CreatePartnerRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            String role,
            @Email String email,
            String phone
    ) {
    }

    public record UpdatePartnerRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            String role,
            @Email String email,
            String phone
    ) {
    }

}
