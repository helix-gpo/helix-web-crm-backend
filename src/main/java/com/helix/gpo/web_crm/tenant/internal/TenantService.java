package com.helix.gpo.web_crm.tenant.internal;

import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos;
import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos.CreateTenantRequest;
import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos.TenantResponse;
import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos.UpdateContactDetailsRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
class TenantService {

    private final TenantRepository tenantRepository;
    private final PartnerRepository partnerRepository;

    TenantResponse create(CreateTenantRequest request) {
        Tenant tenant = Tenant.builder()
                .companyName(request.companyName())
                .legalName(request.legalName())
                .vatId(request.vatId())
                .referenceCode(request.referenceCode())
                .address(request.address())
                .contactEmail(request.contactEmail())
                .contactPhone(request.contactPhone())
                .build();

        return TenantMapper.toResponse(tenantRepository.save(tenant));
    }

    @Transactional(readOnly = true)
    TenantResponse findById(UUID id) {
        return TenantMapper.toResponse(getTenantOrThrow(id));
    }

    @Transactional(readOnly = true)
    List<TenantResponse> findAll() {
        return tenantRepository.findAll().stream()
                .map(TenantMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    List<TenantDtos.PartnerResponse> findPartnersByTenant(UUID tenantId) {
        return partnerRepository.findAllByTenantId(tenantId).stream()
                .map(TenantMapper::toPartnerResponse)
                .toList();
    }

    TenantResponse updateContactDetails(UUID id, UpdateContactDetailsRequest request) {
        Tenant tenant = getTenantOrThrow(id);
        tenant.updateContactDetails(request.contactEmail(), request.contactPhone(), request.address());
        return TenantMapper.toResponse(tenant);
    }

    TenantResponse activate(UUID id) {
        Tenant tenant = getTenantOrThrow(id);
        tenant.activate();
        return TenantMapper.toResponse(tenant);
    }

    TenantResponse archive(UUID id) {
        Tenant tenant = getTenantOrThrow(id);
        tenant.archive();
        return TenantMapper.toResponse(tenant);
    }

    private Tenant getTenantOrThrow(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found: " + id));
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

        return TenantMapper.toPartnerResponse(partnerRepository.save(partner));
    }

    TenantDtos.PartnerResponse updatePartner(UUID partnerId, TenantDtos.UpdatePartnerRequest request) {
        Partner partner = getPartnerOrThrow(partnerId);
        partner.updateDetails(request.firstName(), request.lastName(), request.role(), request.email(), request.phone());
        return TenantMapper.toPartnerResponse(partner);
    }

    void removePartner(UUID partnerId) {
        if (!partnerRepository.existsById(partnerId)) {
            throw new EntityNotFoundException("Partner not found: " + partnerId);
        }
        partnerRepository.deleteById(partnerId);
    }

    private Partner getPartnerOrThrow(UUID partnerId) {
        return partnerRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("Partner not found: " + partnerId));
    }

    TenantResponse updateCoreDetails(UUID id, TenantDtos.UpdateCoreDetailsRequest request) {
        Tenant tenant = getTenantOrThrow(id);
        tenant.updateCoreDetails(request.companyName(), request.legalName(), request.vatId(), request.referenceCode());
        return TenantMapper.toResponse(tenant);
    }

}
