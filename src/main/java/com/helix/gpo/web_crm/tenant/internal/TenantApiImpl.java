package com.helix.gpo.web_crm.tenant.internal;

import com.helix.gpo.web_crm.tenant.api.TenantApi;
import com.helix.gpo.web_crm.tenant.api.TenantSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class TenantApiImpl implements TenantApi {

    private final TenantRepository tenantRepository;

    @Override
    public Optional<TenantSummary> findSummaryById(UUID tenantId) {
        return tenantRepository.findById(tenantId).map(TenantMapper::toSummary);
    }

    @Override
    public boolean existsAndIsActive(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .map(tenant -> tenant.getStatus() == TenantStatus.ACTIVE)
                .orElse(false);
    }

}
