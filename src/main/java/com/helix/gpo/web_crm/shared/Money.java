package com.helix.gpo.web_crm.shared;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
public record Money(
        @Column(precision = 19, scale = 2)
        BigDecimal amount,

        @Column(length = 3)
        String currencyCode
) {

    public Money {
        if (amount != null) {
            amount = amount.setScale(2, RoundingMode.HALF_UP);
        }
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currencyCode);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), this.currencyCode);
    }

    private void requireSameCurrency(Money other) {
        if (!this.currencyCode.equals(other.currencyCode)) {
            throw new IllegalArgumentException("Unterschiedliche Währungen können nicht verrechnet werden: " + this.currencyCode + " / " + other.currencyCode);
        }
    }

}
