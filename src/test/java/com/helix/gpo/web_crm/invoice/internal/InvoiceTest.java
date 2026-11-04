package com.helix.gpo.web_crm.invoice.internal;

import com.helix.gpo.web_crm.shared.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceTest {

    private static final LocalDate ISSUE_DATE = LocalDate.of(2026, 11, 4);

    private Invoice draft() {
        return Invoice.builder().paymentTermsDays(14).build();
    }

    private Invoice draftWithItem() {
        Invoice invoice = draft();
        invoice.addLineItem(LineItemSource.CUSTOM, null, "Beratung", new BigDecimal("2"), "HUR",
                new Money(new BigDecimal("100.00"), "EUR"), new BigDecimal("19.00"));
        return invoice;
    }

    private Invoice issued() {
        Invoice invoice = draftWithItem();
        invoice.issue("RE-2026-00001", ISSUE_DATE);
        return invoice;
    }

    // ---- totals ----

    @Test
    void calculatesTotalsAcrossTaxRates() {
        Invoice invoice = draft();
        invoice.addLineItem(LineItemSource.CUSTOM, null, "Beratung", new BigDecimal("2"), "HUR",
                new Money(new BigDecimal("100.00"), "EUR"), new BigDecimal("19.00"));
        invoice.addLineItem(LineItemSource.CUSTOM, null, "Fachbuch", BigDecimal.ONE, "C62",
                new Money(new BigDecimal("50.00"), "EUR"), new BigDecimal("7.00"));

        assertThat(invoice.netTotal().amount()).isEqualByComparingTo("250.00");
        assertThat(invoice.taxTotal().amount()).isEqualByComparingTo("41.50"); // 38.00 + 3.50
        assertThat(invoice.grossTotal().amount()).isEqualByComparingTo("291.50");
    }

    @Test
    void appliesDefaultsForQuantityUnitAndTax() {
        Invoice invoice = draft();
        InvoiceLineItem item = invoice.addLineItem(LineItemSource.CUSTOM, null, "Pauschale", null, null,
                new Money(new BigDecimal("80.00"), "EUR"), null);

        assertThat(item.getQuantity()).isEqualByComparingTo("1");
        assertThat(item.getUnitCode()).isEqualTo("C62");
        assertThat(item.getTaxRatePercentage()).isEqualByComparingTo("19.00");
        assertThat(item.getPositionNumber()).isEqualTo(1);
    }

    // ---- issue ----

    @Test
    void issueSetsNumberStatusAndDueDate() {
        Invoice invoice = issued();

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(invoice.getInvoiceNumber()).isEqualTo("RE-2026-00001");
        assertThat(invoice.getDueDate()).isEqualTo(ISSUE_DATE.plusDays(14));
    }

    @Test
    void cannotIssueWithoutLineItems() {
        assertThatThrownBy(() -> draft().issue("RE-2026-00001", ISSUE_DATE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotIssueTwice() {
        Invoice invoice = issued();

        assertThatThrownBy(() -> invoice.issue("RE-2026-00002", ISSUE_DATE))
                .isInstanceOf(IllegalStateException.class);
        assertThat(invoice.getInvoiceNumber()).isEqualTo("RE-2026-00001");
    }

    @Test
    void issuedInvoiceIsImmutable() {
        Invoice invoice = issued();

        assertThatThrownBy(() -> invoice.addLineItem(LineItemSource.CUSTOM, null, "Nachtrag", null, null,
                new Money(BigDecimal.TEN, "EUR"), null)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> invoice.updateHeader("REF", 30)).isInstanceOf(IllegalStateException.class);
    }

    // ---- status transitions ----

    @Test
    void followsHappyPathToPaid() {
        Invoice invoice = issued();

        invoice.markSent("kunde@beispiel.de");
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);

        invoice.markPaid(ISSUE_DATE.plusDays(5));
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getPaidDate()).isEqualTo(ISSUE_DATE.plusDays(5));
    }

    @Test
    void overdueInvoiceCanStillBePaid() {
        Invoice invoice = issued();
        invoice.markOverdue();

        invoice.markPaid(ISSUE_DATE.plusDays(30));

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void draftCannotBeSentPaidOrOverdue() {
        Invoice invoice = draftWithItem();

        assertThatThrownBy(() -> invoice.markSent("kunde@beispiel.de")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> invoice.markPaid(ISSUE_DATE)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(invoice::markOverdue).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void paidInvoiceCannotBecomeOverdue() {
        Invoice invoice = issued();
        invoice.markPaid(ISSUE_DATE);

        assertThatThrownBy(invoice::markOverdue).isInstanceOf(IllegalStateException.class);
    }

}
