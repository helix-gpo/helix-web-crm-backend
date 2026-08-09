package com.helix.gpo.web_crm.invoice.internal;

import com.helix.gpo.web_crm.invoice.internal.config.CompanyBillingProperties;
import com.helix.gpo.web_crm.invoice.internal.dto.InvoiceDtos.*;
import com.helix.gpo.web_crm.project.MilestoneSummary;
import com.helix.gpo.web_crm.project.ProjectApi;
import com.helix.gpo.web_crm.shared.Money;
import com.helix.gpo.web_crm.tenant.TenantApi;
import com.helix.gpo.web_crm.tenant.TenantBillingDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;
    private final TenantApi tenantApi;
    private final ProjectApi projectApi;
    private final CompanyBillingProperties companyBillingProperties;

    InvoiceResponse create(CreateInvoiceRequest request) {
        TenantBillingDetails tenant = tenantApi.findBillingDetailsById(request.tenantId())
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found: " + request.tenantId()));

        Invoice invoice = Invoice.builder()
                .tenantId(request.tenantId())
                .projectId(request.projectId())
                .buyerReference(request.buyerReference())
                .paymentTermsDays(request.paymentTermsDays() != null ? request.paymentTermsDays() : 14)
                .seller(InvoiceMapper.toSellerSnapshot(companyBillingProperties))
                .buyer(InvoiceMapper.toBuyerSnapshot(tenant))
                .build();

        request.lineItems().forEach(item -> appendLineItem(invoice, item));

        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    InvoiceResponse addLineItem(UUID invoiceId, LineItemRequest request) {
        Invoice invoice = getInvoiceOrThrow(invoiceId);
        appendLineItem(invoice, request);
        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    InvoiceResponse removeLineItem(UUID invoiceId, UUID lineItemId) {
        Invoice invoice = getInvoiceOrThrow(invoiceId);
        invoice.removeLineItem(lineItemId);
        return InvoiceMapper.toResponse(invoice);
    }

    InvoiceResponse issue(UUID invoiceId, IssueInvoiceRequest request) {
        Invoice invoice = getInvoiceOrThrow(invoiceId);
        LocalDate issueDate = request != null && request.issueDate() != null ? request.issueDate() : LocalDate.now();
        String invoiceNumber = generateInvoiceNumber();
        invoice.issue(invoiceNumber, issueDate);
        return InvoiceMapper.toResponse(invoice);
    }

    @Transactional(readOnly = true)
    InvoiceResponse findById(UUID id) {
        return InvoiceMapper.toResponse(getInvoiceOrThrow(id));
    }

    @Transactional(readOnly = true)
    List<InvoiceResponse> findAllByTenant(UUID tenantId) {
        return invoiceRepository.findAllByTenantId(tenantId).stream()
                .map(InvoiceMapper::toResponse)
                .toList();
    }

    // Genau das, was das Frontend zum Vorbefüllen des Erstell-Formulars braucht:
    // vorgeschlagener Verkäufer (eure Stammdaten), vorgeschlagener Käufer (Mandant),
    // und alle wählbaren Meilensteine mit Preis + Info, ob schon abgerechnet
    @Transactional(readOnly = true)
    InvoicePrefillResponse prefill(UUID tenantId, UUID projectId) {
        TenantBillingDetails tenant = tenantApi.findBillingDetailsById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found: " + tenantId));

        List<MilestoneOptionDto> milestones = projectId == null
                ? List.of()
                : buildMilestoneOptions(projectId);

        return new InvoicePrefillResponse(
                InvoiceMapper.toDto(InvoiceMapper.toSellerSnapshot(companyBillingProperties)),
                InvoiceMapper.toDto(InvoiceMapper.toBuyerSnapshot(tenant)),
                milestones
        );
    }

    private List<MilestoneOptionDto> buildMilestoneOptions(UUID projectId) {
        List<MilestoneSummary> milestones = projectApi.findMilestoneSummariesByProject(projectId);

        List<UUID> milestoneIds = milestones.stream().map(MilestoneSummary::id).toList();
        List<UUID> invoicedMilestoneIds = invoiceLineItemRepository.findAllByMilestoneIdIn(milestoneIds).stream()
                .map(InvoiceLineItem::getMilestoneId)
                .toList();

        return milestones.stream()
                .map(m -> new MilestoneOptionDto(
                        m.id(),
                        m.title(),
                        null, // dueDate nicht in MilestoneSummary enthalten - siehe Hinweis unten
                        m.price(),
                        invoicedMilestoneIds.contains(m.id())
                ))
                .toList();
    }

    private void appendLineItem(Invoice invoice, LineItemRequest request) {
        switch (request.source()) {
            case MILESTONE -> {
                if (request.milestoneId() == null) {
                    throw new IllegalArgumentException("milestoneId is required for MILESTONE line items");
                }
                MilestoneSummary milestone = projectApi.findMilestoneSummaryById(request.milestoneId())
                        .orElseThrow(() -> new EntityNotFoundException("Milestone not found: " + request.milestoneId()));

                Money unitPrice = request.unitPrice() != null ? request.unitPrice() : milestone.price();
                if (unitPrice == null) {
                    throw new IllegalArgumentException(
                            "Milestone has no price and none was provided: " + request.milestoneId());
                }

                invoice.addLineItem(
                        LineItemSource.MILESTONE,
                        milestone.id(),
                        request.description() != null ? request.description() : milestone.title(),
                        request.quantity(),
                        request.unitCode(),
                        unitPrice,
                        request.taxRatePercentage()
                );
            }
            case CUSTOM -> {
                if (request.description() == null || request.unitPrice() == null) {
                    throw new IllegalArgumentException("description and unitPrice are required for CUSTOM line items");
                }
                invoice.addLineItem(
                        LineItemSource.CUSTOM,
                        null,
                        request.description(),
                        request.quantity(),
                        request.unitCode(),
                        request.unitPrice(),
                        request.taxRatePercentage()
                );
            }
        }
    }

    private String generateInvoiceNumber() {
        // Platzhalter - lückenlose Sequenz bauen wir, sobald wir konkret XRechnung umsetzen
        return "INV-" + LocalDate.now().getYear() + "-" + System.currentTimeMillis();
    }

    private Invoice getInvoiceOrThrow(UUID id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + id));
    }

}
