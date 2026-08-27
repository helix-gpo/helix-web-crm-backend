package com.helix.gpo.web_crm.invoice.internal;

import com.helix.gpo.web_crm.invoice.internal.config.CompanyBillingProperties;
import com.helix.gpo.web_crm.invoice.internal.dto.InvoiceDtos.*;
import com.helix.gpo.web_crm.tenant.TenantBillingDetails;

final class InvoiceMapper {

    private InvoiceMapper() {
    }

    static BillingParty toSellerSnapshot(CompanyBillingProperties company) {
        return new BillingParty(
                company.name(),
                company.vatId(),
                company.address(),
                company.email(),
                company.iban(),
                company.bic()
        );
    }

    static BillingParty toBuyerSnapshot(TenantBillingDetails tenant) {
        String name = tenant.legalName() != null ? tenant.legalName() : tenant.companyName();
        return new BillingParty(name, tenant.vatId(), tenant.address(), tenant.contactEmail(), null, null);
    }

    static BillingPartyDto toDto(BillingParty party) {
        if (party == null) return null;
        return new BillingPartyDto(
                party.name(), party.vatId(), party.address(), party.email(), party.iban(), party.bic()
        );
    }

    static InvoiceLineItemResponse toLineItemResponse(InvoiceLineItem item) {
        return new InvoiceLineItemResponse(
                item.getId(),
                item.getPositionNumber(),
                item.getSource(),
                item.getMilestoneId(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitCode(),
                item.getUnitPrice(),
                item.getTaxRatePercentage(),
                item.netAmount(),
                item.taxAmount(),
                item.grossAmount()
        );
    }

    static InvoiceResponse toResponse(Invoice invoice) {
        return toResponse(invoice, invoice.getSeller(), invoice.getBuyer());
    }

    static InvoiceResponse toResponse(Invoice invoice, BillingParty seller, BillingParty buyer) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getTenantId(),
                invoice.getProjectId(),
                invoice.getStatus(),
                invoice.getCurrencyCode(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getPaymentTermsDays(),
                invoice.getBuyerReference(),
                toDto(seller),
                toDto(buyer),
                invoice.getLineItems().stream().map(InvoiceMapper::toLineItemResponse).toList(),
                invoice.netTotal(),
                invoice.taxTotal(),
                invoice.grossTotal(),
                invoice.getDocumentKey(),
                invoice.getSentToEmail(),
                invoice.getSentAt(),
                invoice.getPaidDate(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }

}
