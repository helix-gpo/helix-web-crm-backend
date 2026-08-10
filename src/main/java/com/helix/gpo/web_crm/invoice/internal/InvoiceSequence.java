package com.helix.gpo.web_crm.invoice.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "invoice_sequences")
class InvoiceSequence {

    @Id
    @Column(name = "sequence_year")
    private int year;

    @Column(name = "last_value", nullable = false)
    private long lastValue;

    InvoiceSequence(int year) {
        this.year = year;
        this.lastValue = 0;
    }

    long nextValue() {
        this.lastValue++;
        return this.lastValue;
    }

}
