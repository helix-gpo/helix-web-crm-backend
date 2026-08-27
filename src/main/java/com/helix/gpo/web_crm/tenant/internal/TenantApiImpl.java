package com.helix.gpo.web_crm.tenant.internal;

import com.helix.gpo.web_crm.storage.StorageApi;
import com.helix.gpo.web_crm.tenant.PartnerSummary;
import com.helix.gpo.web_crm.tenant.TenantApi;
import com.helix.gpo.web_crm.tenant.TenantBillingDetails;
import com.helix.gpo.web_crm.tenant.TenantSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class TenantApiImpl implements TenantApi {

    private final TenantRepository tenantRepository;
    private final PartnerRepository partnerRepository;
    private final StorageApi storageApi;

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

    @Override
    public Optional<TenantBillingDetails> findBillingDetailsById(UUID tenantId) {
        return tenantRepository.findById(tenantId).map(TenantMapper::toBillingDetails);
    }

    @Override
    public Optional<PartnerSummary> findPartnerSummaryById(UUID partnerId) {
        return partnerRepository.findById(partnerId).map(partner -> {
            String photoUrl = partner.getPhotoKey() != null
                    ? storageApi.presignedUrl(partner.getPhotoKey(), Duration.ofMinutes(30))
                    : null;
            return TenantMapper.toPartnerSummary(partner, photoUrl);
        });
    }

}
