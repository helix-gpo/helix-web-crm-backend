package com.helix.gpo.web_crm.tenant.internal;

import com.helix.gpo.web_crm.shared.ImageUploadValidator;
import com.helix.gpo.web_crm.storage.StorageApi;
import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos;
import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos.CreateTenantRequest;
import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos.TenantResponse;
import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos.UpdateContactDetailsRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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

    TenantResponse create(CreateTenantRequest request) {
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
        return toResponse(getTenantOrThrow(id));
    }

    @Transactional(readOnly = true)
    List<TenantResponse> findAll() {
        return tenantRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    List<TenantDtos.PartnerResponse> findPartnersByTenant(UUID tenantId) {
        return partnerRepository.findAllByTenantId(tenantId).stream()
                .map(this::toPartnerResponse)
                .toList();
    }

    TenantResponse updateContactDetails(UUID id, UpdateContactDetailsRequest request) {
        Tenant tenant = getTenantOrThrow(id);
        tenant.updateContactDetails(request.contactEmail(), request.contactPhone(), request.address(), request.websiteUrl());
        return toResponse(tenant);
    }

    TenantResponse updateNotes(UUID id, TenantDtos.UpdateNotesRequest request) {
        Tenant tenant = getTenantOrThrow(id);
        tenant.updateNotes(request.notes());
        return toResponse(tenant);
    }

    TenantResponse activate(UUID id) {
        Tenant tenant = getTenantOrThrow(id);
        tenant.activate();
        return toResponse(tenant);
    }

    TenantResponse archive(UUID id) {
        Tenant tenant = getTenantOrThrow(id);
        tenant.archive();
        return toResponse(tenant);
    }

    TenantResponse updateCoreDetails(UUID id, TenantDtos.UpdateCoreDetailsRequest request) {
        Tenant tenant = getTenantOrThrow(id);
        tenant.updateCoreDetails(request.companyName(), request.legalName(), request.vatId(), request.referenceCode());
        return toResponse(tenant);
    }

    TenantResponse uploadLogo(UUID tenantId, MultipartFile file) {
        Tenant tenant = getTenantOrThrow(tenantId);
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
        Tenant tenant = getTenantOrThrow(tenantId);
        if (tenant.getLogoKey() != null) {
            storageApi.delete(tenant.getLogoKey());
            tenant.removeLogo();
        }
        return toResponse(tenant);
    }

    TenantDtos.PartnerResponse addPartner(UUID tenantId, TenantDtos.CreatePartnerRequest request) {
        Tenant tenant = getTenantOrThrow(tenantId);

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
        Partner partner = getPartnerOrThrow(partnerId);
        partner.updateDetails(request.firstName(), request.lastName(), request.role(), request.email(), request.phone());
        return toPartnerResponse(partner);
    }

    void removePartner(UUID partnerId) {
        Partner partner = getPartnerOrThrow(partnerId);
        if (partner.getPhotoKey() != null) {
            storageApi.delete(partner.getPhotoKey());
        }
        partnerRepository.deleteById(partnerId);
    }

    TenantDtos.PartnerResponse uploadPartnerPhoto(UUID partnerId, MultipartFile file) {
        Partner partner = getPartnerOrThrow(partnerId);
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
        Partner partner = getPartnerOrThrow(partnerId);
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

}
