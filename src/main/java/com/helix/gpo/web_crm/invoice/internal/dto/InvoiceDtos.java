package com.helix.gpo.web_crm.invoice.internal.dto;

import com.helix.gpo.web_crm.invoice.internal.InvoiceStatus;
import com.helix.gpo.web_crm.invoice.internal.LineItemSource;
import com.helix.gpo.web_crm.shared.Address;
import com.helix.gpo.web_crm.shared.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class InvoiceDtos {

    private InvoiceDtos() {
    }

    public record BillingPartyDto(
            String name,
            String vatId,
            Address address,
            String email,
            String iban,
            String bic
    ) {
    }

    public record LineItemRequest(
            @NotNull LineItemSource source,
            UUID milestoneId,
            String description,
            BigDecimal quantity,
            String unitCode,
            Money unitPrice,
            BigDecimal taxRatePercentage
    ) {
    }

    public record CreateInvoiceRequest(
            @NotNull UUID tenantId,
            UUID projectId,
            String buyerReference,
            Integer paymentTermsDays,
            @NotEmpty @Valid List<LineItemRequest> lineItems
    ) {
    }

    public record IssueInvoiceRequest(
            LocalDate issueDate
    ) {
    }

    public record InvoiceLineItemResponse(
            UUID id,
            int positionNumber,
            LineItemSource source,
            UUID milestoneId,
            String description,
            BigDecimal quantity,
            String unitCode,
            Money unitPrice,
            BigDecimal taxRatePercentage,
            Money netAmount,
            Money taxAmount,
            Money grossAmount
    ) {
    }

    public record InvoiceResponse(
            UUID id,
            String invoiceNumber,
            UUID tenantId,
            UUID projectId,
            InvoiceStatus status,
            String currencyCode,
            LocalDate issueDate,
            LocalDate dueDate,
            Integer paymentTermsDays,
            String buyerReference,
            BillingPartyDto seller,
            BillingPartyDto buyer,
            List<InvoiceLineItemResponse> lineItems,
            Money netTotal,
            Money taxTotal,
            Money grossTotal,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    // ---- Prefill: alles, was das Frontend braucht, um das Erstell-Formular zu befüllen ----

    public record MilestoneOptionDto(
            UUID id,
            String title,
            LocalDate dueDate,
            Money price,
            boolean alreadyInvoiced,
            boolean inDraftInvoice,
            String milestoneStatus
    ) {
    }

    public record InvoicePrefillResponse(
            BillingPartyDto suggestedSeller,
            BillingPartyDto suggestedBuyer,
            List<MilestoneOptionDto> availableMilestones
    ) {
    }

    public record UpdateInvoiceHeaderRequest(
            String buyerReference,
            Integer paymentTermsDays
    ) {
    }

    public record UpdateLineItemRequest(
            @NotBlank String description,
            BigDecimal quantity,
            String unitCode,
            @NotNull Money unitPrice,
            BigDecimal taxRatePercentage
    ) {
    }

}
