package com.helix.gpo.web_crm.invoice.internal;

import com.helix.gpo.web_crm.shared.BaseEntity;
import com.helix.gpo.web_crm.shared.Money;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "invoices")
class Invoice extends BaseEntity {

    @Column(name = "invoice_number", unique = true, length = 40)
    private String invoiceNumber;

    // Rohe IDs zu tenant/project - für Verknüpfung/Filterung im CRM,
    // NICHT die Rechtsgrundlage der Rechnung (dafür: seller/buyer-Snapshot unten)
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "project_id")
    private UUID projectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "currency_code", nullable = false, length = 3)
    @Builder.Default
    private String currencyCode = "EUR";

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "payment_terms_days")
    @Builder.Default
    private Integer paymentTermsDays = 14;

    // Leitweg-ID o.ä. - v.a. für B2G-Rechnungen relevant, bei B2B optional
    @Column(name = "buyer_reference", length = 60)
    private String buyerReference;

    @Column(name = "sent_to_email", length = 320)
    private String sentToEmail;

    @Column(name = "sent_at")
    private Instant sentAt;

    public void markSent(String sentToEmail) {
        if (this.status != InvoiceStatus.ISSUED) {
            throw new IllegalStateException(
                    "Nur ausgestellte Rechnungen können versendet werden: " + getId());
        }
        this.sentToEmail = sentToEmail;
        this.sentAt = java.time.Instant.now();
        this.status = InvoiceStatus.SENT;
    }

    // Unveränderlicher Schnappschuss zum Ausstellungszeitpunkt - GENAU deswegen
    // eigene Embeddables statt Live-Referenz auf tenant/CompanyBillingProperties
    @Embedded
    @AttributeOverride(name = "name", column = @Column(name = "seller_name"))
    @AttributeOverride(name = "vatId", column = @Column(name = "seller_vat_id"))
    @AttributeOverride(name = "email", column = @Column(name = "seller_email"))
    @AttributeOverride(name = "iban", column = @Column(name = "seller_iban"))
    @AttributeOverride(name = "bic", column = @Column(name = "seller_bic"))
    @AttributeOverride(name = "address.street", column = @Column(name = "seller_street"))
    @AttributeOverride(name = "address.houseNumber", column = @Column(name = "seller_house_number"))
    @AttributeOverride(name = "address.postalCode", column = @Column(name = "seller_postal_code"))
    @AttributeOverride(name = "address.city", column = @Column(name = "seller_city"))
    @AttributeOverride(name = "address.countryCode", column = @Column(name = "seller_country_code"))
    private BillingParty seller;

    @Embedded
    @AttributeOverride(name = "name", column = @Column(name = "buyer_name"))
    @AttributeOverride(name = "vatId", column = @Column(name = "buyer_vat_id"))
    @AttributeOverride(name = "email", column = @Column(name = "buyer_email"))
    @AttributeOverride(name = "iban", column = @Column(name = "buyer_iban"))
    @AttributeOverride(name = "bic", column = @Column(name = "buyer_bic"))
    @AttributeOverride(name = "address.street", column = @Column(name = "buyer_street"))
    @AttributeOverride(name = "address.houseNumber", column = @Column(name = "buyer_house_number"))
    @AttributeOverride(name = "address.postalCode", column = @Column(name = "buyer_postal_code"))
    @AttributeOverride(name = "address.city", column = @Column(name = "buyer_city"))
    @AttributeOverride(name = "address.countryCode", column = @Column(name = "buyer_country_code"))
    private BillingParty buyer;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("positionNumber ASC")
    @Builder.Default
    private List<InvoiceLineItem> lineItems = new ArrayList<>();

    public InvoiceLineItem addLineItem(LineItemSource source, UUID milestoneId, String description,
                                       java.math.BigDecimal quantity, String unitCode,
                                       Money unitPrice, java.math.BigDecimal taxRatePercentage) {
        requireDraft();

        InvoiceLineItem lineItem = InvoiceLineItem.builder()
                .invoice(this)
                .positionNumber(this.lineItems.size() + 1)
                .source(source)
                .milestoneId(milestoneId)
                .description(description)
                .quantity(quantity != null ? quantity : java.math.BigDecimal.ONE)
                .unitCode(unitCode != null ? unitCode : "C62")
                .unitPrice(unitPrice)
                .taxRatePercentage(taxRatePercentage != null ? taxRatePercentage : new java.math.BigDecimal("19.00"))
                .build();

        this.lineItems.add(lineItem);
        return lineItem;
    }

    public void removeLineItem(UUID lineItemId) {
        requireDraft();
        this.lineItems.removeIf(item -> item.getId().equals(lineItemId));
        renumberPositions();
    }

    private void renumberPositions() {
        for (int i = 0; i < lineItems.size(); i++) {
            lineItems.get(i).setPositionNumber(i + 1);
        }
    }

    public Money netTotal() {
        return sumOf(InvoiceLineItem::netAmount);
    }

    public Money taxTotal() {
        return sumOf(InvoiceLineItem::taxAmount);
    }

    public Money grossTotal() {
        return sumOf(InvoiceLineItem::grossAmount);
    }

    private Money sumOf(java.util.function.Function<InvoiceLineItem, Money> extractor) {
        return lineItems.stream()
                .map(extractor)
                .reduce(new Money(java.math.BigDecimal.ZERO, currencyCode), Money::add);
    }

    @Column(name = "document_key", length = 200)
    private String documentKey;

    public void attachDocument(String documentKey) {
        this.documentKey = documentKey;
    }

    public void issue(String invoiceNumber, LocalDate issueDate) {
        if (this.lineItems.isEmpty()) {
            throw new IllegalStateException("Cannot issue an invoice without line items: " + getId());
        }
        this.invoiceNumber = invoiceNumber;
        this.issueDate = issueDate;
        this.dueDate = issueDate.plusDays(this.paymentTermsDays != null ? this.paymentTermsDays : 14);
        this.status = InvoiceStatus.ISSUED;
    }

    private void requireDraft() {
        if (this.status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Cannot modify line items on a non-draft invoice: " + getId());
        }
    }
    public void updateLineItem(UUID lineItemId, String description, BigDecimal quantity,
                               String unitCode, Money unitPrice, BigDecimal taxRatePercentage) {
        requireDraft();

        InvoiceLineItem item = this.lineItems.stream()
                .filter(li -> li.getId().equals(lineItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Line item not found: " + lineItemId));

        if (description == null || unitPrice == null) {
            throw new IllegalArgumentException("description and unitPrice are required");
        }

        item.setDescription(description);
        item.setQuantity(quantity != null ? quantity : BigDecimal.ONE);
        item.setUnitCode(unitCode != null ? unitCode : "C62");
        item.setUnitPrice(unitPrice);
        item.setTaxRatePercentage(taxRatePercentage != null ? taxRatePercentage : new BigDecimal("19.00"));
    }

    public void updateHeader(String buyerReference, Integer paymentTermsDays) {
        requireDraft();
        this.buyerReference = buyerReference;
        this.paymentTermsDays = paymentTermsDays != null ? paymentTermsDays : 14;
    }

}
