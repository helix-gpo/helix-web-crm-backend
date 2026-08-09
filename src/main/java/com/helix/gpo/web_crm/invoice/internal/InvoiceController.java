package com.helix.gpo.web_crm.invoice.internal;

import com.helix.gpo.web_crm.invoice.internal.dto.InvoiceDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/crm/invoices")
class InvoiceController {

    private final InvoiceService invoiceService;

    InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/prefill")
    InvoicePrefillResponse prefill(@RequestParam UUID tenantId,
                                   @RequestParam(required = false) UUID projectId) {
        return invoiceService.prefill(tenantId, projectId);
    }

    @PostMapping
    ResponseEntity<InvoiceResponse> create(@Valid @RequestBody CreateInvoiceRequest request) {
        InvoiceResponse response = invoiceService.create(request);
        return ResponseEntity.created(URI.create("/api/crm/invoices/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    InvoiceResponse findById(@PathVariable UUID id) {
        return invoiceService.findById(id);
    }

    @GetMapping
    List<InvoiceResponse> findAllByTenant(@RequestParam UUID tenantId) {
        return invoiceService.findAllByTenant(tenantId);
    }

    @PostMapping("/{id}/line-items")
    InvoiceResponse addLineItem(@PathVariable UUID id, @Valid @RequestBody LineItemRequest request) {
        return invoiceService.addLineItem(id, request);
    }

    @DeleteMapping("/{id}/line-items/{lineItemId}")
    InvoiceResponse removeLineItem(@PathVariable UUID id, @PathVariable UUID lineItemId) {
        return invoiceService.removeLineItem(id, lineItemId);
    }

    @PostMapping("/{id}/issue")
    InvoiceResponse issue(@PathVariable UUID id, @RequestBody(required = false) IssueInvoiceRequest request) {
        return invoiceService.issue(id, request);
    }

}
