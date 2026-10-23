package com.helix.gpo.web_crm.tenant.internal;

import com.helix.gpo.web_crm.storage.StorageApi;
import com.helix.gpo.web_crm.tenant.PartnerSummary;
import com.helix.gpo.web_crm.tenant.TenantApi;
import com.helix.gpo.web_crm.tenant.TenantBillingDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class TenantApiImpl implements TenantApi {

    private final TenantRepository tenantRepository;
    private final PartnerRepository partnerRepository;

    private final StorageApi storageApi;

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
        return partnerRepository.findById(partnerId).map(this::toPartnerSummary);
    }

    @Override
    public Map<UUID, PartnerSummary> findPartnerSummariesByIds(Collection<UUID> partnerIds) {
        if (partnerIds.isEmpty()) {
            return Map.of();
        }
        return partnerRepository.findAllById(partnerIds).stream()
                .collect(Collectors.toMap(Partner::getId, this::toPartnerSummary));
    }

    private PartnerSummary toPartnerSummary(Partner partner) {
        String photoUrl = partner.getPhotoKey() != null
                ? storageApi.presignedUrl(partner.getPhotoKey(), Duration.ofMinutes(30))
                : null;
        return TenantMapper.toPartnerSummary(partner, photoUrl);
    }

    @Override
    public Optional<String> findCreatedBy(UUID tenantId) {
        return tenantRepository.findById(tenantId).map(Tenant::getCreatedBy);
    }

}
