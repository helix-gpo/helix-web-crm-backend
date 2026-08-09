package com.helix.gpo.web_crm.invoice.internal;

import com.helix.gpo.web_crm.shared.BaseEntity;
import com.helix.gpo.web_crm.shared.Money;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "invoice_line_items")
class InvoiceLineItem extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "invoice_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_line_item_invoice"))
    private Invoice invoice;

    @Column(name = "position_number", nullable = false)
    private int positionNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LineItemSource source;

    // Nur befüllt, wenn source == MILESTONE - rohe UUID, keine Cross-Modul-Relation
    @Column(name = "milestone_id")
    private UUID milestoneId;

    @Column(nullable = false, length = 300)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ONE;

    // UN/CEFACT Mengeneinheiten-Code, Pflichtfeld in XRechnung (z.B. "C62" = Stück, "HUR" = Stunde, "DAY" = Tag)
    @Column(name = "unit_code", nullable = false, length = 10)
    @Builder.Default
    private String unitCode = "C62";

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount", nullable = false))
    @AttributeOverride(name = "currencyCode", column = @Column(name = "unit_price_currency", nullable = false))
    private Money unitPrice;

    // Prozentsatz, nicht der Steuerbetrag selbst - der wird abgeleitet berechnet
    @Column(name = "tax_rate_percentage", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRatePercentage = new BigDecimal("19.00");

    public Money netAmount() {
        return unitPrice.multiply(quantity);
    }

    public Money taxAmount() {
        return netAmount().multiply(taxRatePercentage.divide(new BigDecimal("100"), RoundingMode.HALF_UP));
    }

    public Money grossAmount() {
        return netAmount().add(taxAmount());
    }

}
