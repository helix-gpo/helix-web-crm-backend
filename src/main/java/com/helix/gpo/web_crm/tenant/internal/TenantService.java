package com.helix.gpo.web_crm.tenant.internal;

import com.helix.gpo.web_crm.access.AccessApi;
import com.helix.gpo.web_crm.access.EntityType;
import com.helix.gpo.web_crm.shared.ImageUploadValidator;
import com.helix.gpo.web_crm.storage.StorageApi;
import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos;
import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos.CreateTenantRequest;
import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos.TenantResponse;
import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos.UpdateContactDetailsRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
class TenantService {

    private final TenantRepository tenantRepository;
    private final PartnerRepository partnerRepository;

    private final StorageApi storageApi;
    private final AccessApi accessApi;

    TenantResponse create(CreateTenantRequest request) {
        accessApi.requireWrite(EntityType.TENANT);

        Tenant tenant = Tenant.builder()
                .companyName(request.companyName())
                .legalName(request.legalName())
                .vatId(request.vatId())
                .referenceCode(request.referenceCode())
                .address(request.address())
                .contactEmail(request.contactEmail())
                .contactPhone(request.contactPhone())
                .websiteUrl(request.websiteUrl())
                .build();

        return toResponse(tenantRepository.save(tenant));
    }

    @Transactional(readOnly = true)
    TenantResponse findById(UUID id) {
        accessApi.requireRead(EntityType.TENANT);
        Tenant tenant = getTenantOrThrow(id);
        requireTenantAccess(tenant);
        return toResponse(tenant);
    }

    @Transactional(readOnly = true)
    List<TenantResponse> findAll() {
        accessApi.requireRead(EntityType.TENANT);
        return filterAccessible(tenantRepository.findAll()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    List<TenantDtos.PartnerResponse> findPartnersByTenant(UUID tenantId) {
        accessApi.requireRead(EntityType.PARTNER);
        Tenant tenant = getTenantOrThrow(tenantId);
        requireTenantAccess(tenant);

        return partnerRepository.findAllByTenantId(tenantId).stream()
                .map(this::toPartnerResponse)
                .toList();
    }

    TenantResponse updateContactDetails(UUID id, UpdateContactDetailsRequest request) {
        accessApi.requireWrite(EntityType.TENANT);
        Tenant tenant = getTenantOrThrow(id);
        requireTenantAccess(tenant);

        tenant.updateContactDetails(request.contactEmail(), request.contactPhone(), request.address(), request.websiteUrl());
        return toResponse(tenant);
    }

    TenantResponse updateNotes(UUID id, TenantDtos.UpdateNotesRequest request) {
        accessApi.requireWrite(EntityType.TENANT);
        Tenant tenant = getTenantOrThrow(id);
        requireTenantAccess(tenant);

        tenant.updateNotes(request.notes());
        return toResponse(tenant);
    }

    TenantResponse activate(UUID id) {
        accessApi.requireWrite(EntityType.TENANT);
        Tenant tenant = getTenantOrThrow(id);
        requireTenantAccess(tenant);

        tenant.activate();
        return toResponse(tenant);
    }

    TenantResponse archive(UUID id) {
        accessApi.requireWrite(EntityType.TENANT);
        Tenant tenant = getTenantOrThrow(id);
        requireTenantAccess(tenant);

        tenant.archive();
        return toResponse(tenant);
    }

    TenantResponse updateCoreDetails(UUID id, TenantDtos.UpdateCoreDetailsRequest request) {
        accessApi.requireWrite(EntityType.TENANT);
        Tenant tenant = getTenantOrThrow(id);
        requireTenantAccess(tenant);

        tenant.updateCoreDetails(request.companyName(), request.legalName(), request.vatId(), request.referenceCode());
        return toResponse(tenant);
    }

    TenantResponse uploadLogo(UUID tenantId, MultipartFile file) {
        accessApi.requireWrite(EntityType.TENANT);
        Tenant tenant = getTenantOrThrow(tenantId);
        requireTenantAccess(tenant);

        ImageUploadValidator.validate(file);

        if (tenant.getLogoKey() != null) {
            storageApi.delete(tenant.getLogoKey());
        }

        String key = ImageUploadValidator.generateKey("tenant-logos", tenantId, file);
        try {
            storageApi.upload(key, file.getBytes(), file.getContentType());
        } catch (IOException e) {
            throw new IllegalStateException("Logo konnte nicht hochgeladen werden.", e);
        }

        tenant.attachLogo(key);
        return toResponse(tenant);
    }

    TenantResponse removeLogo(UUID tenantId) {
        accessApi.requireWrite(EntityType.TENANT);
        Tenant tenant = getTenantOrThrow(tenantId);
        requireTenantAccess(tenant);

        if (tenant.getLogoKey() != null) {
            storageApi.delete(tenant.getLogoKey());
            tenant.removeLogo();
        }
        return toResponse(tenant);
    }

    TenantDtos.PartnerResponse addPartner(UUID tenantId, TenantDtos.CreatePartnerRequest request) {
        accessApi.requireWrite(EntityType.PARTNER);
        Tenant tenant = getTenantOrThrow(tenantId);
        requireTenantAccess(tenant);

        Partner partner = Partner.builder()
                .tenant(tenant)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(request.role())
                .email(request.email())
                .phone(request.phone())
                .build();

        return toPartnerResponse(partnerRepository.save(partner));
    }

    TenantDtos.PartnerResponse updatePartner(UUID partnerId, TenantDtos.UpdatePartnerRequest request) {
        accessApi.requireWrite(EntityType.PARTNER);
        Partner partner = getPartnerOrThrow(partnerId);
        requireTenantAccess(partner.getTenant());

        partner.updateDetails(request.firstName(), request.lastName(), request.role(), request.email(), request.phone());
        return toPartnerResponse(partner);
    }

    void removePartner(UUID partnerId) {
        accessApi.requireDelete(EntityType.PARTNER);
        Partner partner = getPartnerOrThrow(partnerId);
        requireTenantAccess(partner.getTenant());

        if (partner.getPhotoKey() != null) {
            storageApi.delete(partner.getPhotoKey());
        }
        partnerRepository.deleteById(partnerId);
    }

    TenantDtos.PartnerResponse uploadPartnerPhoto(UUID partnerId, MultipartFile file) {
        accessApi.requireWrite(EntityType.PARTNER);
        Partner partner = getPartnerOrThrow(partnerId);
        requireTenantAccess(partner.getTenant());

        ImageUploadValidator.validate(file);

        if (partner.getPhotoKey() != null) {
            storageApi.delete(partner.getPhotoKey());
        }

        String key = ImageUploadValidator.generateKey("partner-photos", partnerId, file);
        try {
            storageApi.upload(key, file.getBytes(), file.getContentType());
        } catch (IOException e) {
            throw new IllegalStateException("Foto konnte nicht hochgeladen werden.", e);
        }

        partner.attachPhoto(key);
        return toPartnerResponse(partner);
    }

    TenantDtos.PartnerResponse removePartnerPhoto(UUID partnerId) {
        accessApi.requireWrite(EntityType.PARTNER);
        Partner partner = getPartnerOrThrow(partnerId);
        requireTenantAccess(partner.getTenant());

        if (partner.getPhotoKey() != null) {
            storageApi.delete(partner.getPhotoKey());
            partner.removePhoto();
        }
        return toPartnerResponse(partner);
    }

    private Tenant getTenantOrThrow(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dieser Mandant wurde nicht gefunden."));
    }

    private Partner getPartnerOrThrow(UUID partnerId) {
        return partnerRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("Dieser Ansprechpartner wurde nicht gefunden."));
    }

    private TenantResponse toResponse(Tenant tenant) {
        String logoUrl = tenant.getLogoKey() != null
                ? storageApi.presignedUrl(tenant.getLogoKey(), Duration.ofMinutes(30))
                : null;
        return TenantMapper.toResponse(tenant, logoUrl);
    }

    private TenantDtos.PartnerResponse toPartnerResponse(Partner partner) {
        String photoUrl = partner.getPhotoKey() != null
                ? storageApi.presignedUrl(partner.getPhotoKey(), Duration.ofMinutes(30))
                : null;
        return TenantMapper.toPartnerResponse(partner, photoUrl);
    }

    private List<Tenant> filterAccessible(List<Tenant> tenants) {
        if (accessApi.isUnrestricted()) {
            return tenants;
        }
        List<UUID> accessibleIds = accessApi.accessibleTenantIds();
        return tenants.stream()
                .filter(t -> accessibleIds.contains(t.getId()) || isSelfCreated(t.getCreatedBy()))
                .toList();
    }

    private void requireTenantAccess(Tenant tenant) {
        boolean allowed = accessApi.isUnrestricted()
                || accessApi.canAccessTenant(tenant.getId())
                || isSelfCreated(tenant.getCreatedBy());
        if (!allowed) {
            throw new AccessDeniedException("Kein Zugriff auf diesen Mandanten.");
        }
    }

    private boolean isSelfCreated(String createdBy) {
        String me = accessApi.currentUserEmail();
        return createdBy != null && createdBy.equalsIgnoreCase(me);
    }

}
