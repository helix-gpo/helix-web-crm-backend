package com.helix.gpo.web_crm.tenant.internal;

import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crm/tenants")
class TenantController {

    private final TenantService tenantService;

    @PostMapping
    ResponseEntity<TenantResponse> create(@Valid @RequestBody CreateTenantRequest request) {
        TenantResponse response = tenantService.create(request);
        return ResponseEntity.created(URI.create("/api/crm/tenants/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    TenantResponse findById(@PathVariable UUID id) {
        return tenantService.findById(id);
    }

    @GetMapping
    List<TenantResponse> findAll() {
        return tenantService.findAll();
    }

    @GetMapping("/{id}/partners")
    List<PartnerResponse> findPartners(@PathVariable UUID id) {
        return tenantService.findPartnersByTenant(id);
    }

    @PatchMapping("/{id}/contact-details")
    TenantResponse updateContactDetails(@PathVariable UUID id,
                                        @Valid @RequestBody UpdateContactDetailsRequest request) {
        return tenantService.updateContactDetails(id, request);
    }

    @PostMapping("/{id}/activate")
    TenantResponse activate(@PathVariable UUID id) {
        return tenantService.activate(id);
    }

    @PostMapping("/{id}/archive")
    @ResponseStatus(HttpStatus.OK)
    TenantResponse archive(@PathVariable UUID id) {
        return tenantService.archive(id);
    }

    @PostMapping("/{id}/partners")
    ResponseEntity<PartnerResponse> addPartner(@PathVariable UUID id,
                                               @Valid @RequestBody CreatePartnerRequest request) {
        PartnerResponse response = tenantService.addPartner(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{tenantId}/partners/{partnerId}")
    PartnerResponse updatePartner(@PathVariable UUID tenantId, @PathVariable UUID partnerId,
                                  @Valid @RequestBody UpdatePartnerRequest request) {
        return tenantService.updatePartner(partnerId, request);
    }

    @DeleteMapping("/{tenantId}/partners/{partnerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removePartner(@PathVariable UUID tenantId, @PathVariable UUID partnerId) {
        tenantService.removePartner(partnerId);
    }

    @PatchMapping("/{id}/core-details")
    TenantResponse updateCoreDetails(@PathVariable UUID id,
                                     @Valid @RequestBody UpdateCoreDetailsRequest request) {
        return tenantService.updateCoreDetails(id, request);
    }

}
