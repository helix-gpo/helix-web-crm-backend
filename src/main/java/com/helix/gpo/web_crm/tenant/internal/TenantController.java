package com.helix.gpo.web_crm.tenant.internal;

import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos.CreateTenantRequest;
import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos.TenantResponse;
import com.helix.gpo.web_crm.tenant.internal.dto.TenantDtos.UpdateContactDetailsRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/crm/tenants")
@RequiredArgsConstructor
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

}
