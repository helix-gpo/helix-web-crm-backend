package com.helix.gpo.web_crm.tenant.internal;

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

    TenantResponse create(CreateTenantRequest request) {
        Tenant tenant = Tenant.builder()
                .companyName(request.companyName())
                .legalName(request.legalName())
                .vatId(request.vatId())
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

}
