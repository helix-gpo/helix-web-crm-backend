package com.helix.gpo.web_crm.invoice.internal;

import com.helix.gpo.web_crm.invoice.internal.dto.InvoiceDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crm/invoices")
class InvoiceController {

    private final InvoiceService invoiceService;

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

    // Ersetzt die alte findAllByTenant(@RequestParam UUID tenantId) - genau
    // die Doppelbelegung war die Ursache des Ambiguous-Mapping-Fehlers
    @GetMapping
    List<InvoiceResponse> findAll(@RequestParam(required = false) UUID tenantId) {
        return tenantId != null ? invoiceService.findAllByTenant(tenantId) : invoiceService.findAll();
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

    @PatchMapping("/{id}")
    InvoiceResponse updateHeader(@PathVariable UUID id, @Valid @RequestBody UpdateInvoiceHeaderRequest request) {
        return invoiceService.updateHeader(id, request);
    }

    @PatchMapping("/{id}/line-items/{lineItemId}")
    InvoiceResponse updateLineItem(@PathVariable UUID id, @PathVariable UUID lineItemId,
                                   @Valid @RequestBody UpdateLineItemRequest request) {
        return invoiceService.updateLineItem(id, lineItemId, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        invoiceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/document")
    Map<String, String> documentUrl(@PathVariable UUID id) {
        return Map.of("url", invoiceService.getDocumentUrl(id));
    }

    @PostMapping("/{id}/send")
    InvoiceResponse send(@PathVariable UUID id, @RequestBody(required = false) SendInvoiceRequest request) {
        return invoiceService.send(id, request);
    }

    @PostMapping("/{id}/mark-paid")
    InvoiceResponse markPaid(@PathVariable UUID id, @RequestBody(required = false) MarkPaidRequest request) {
        return invoiceService.markPaid(id, request);
    }

}
