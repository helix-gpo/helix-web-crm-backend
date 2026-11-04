package com.helix.gpo.web_crm.shared;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void roundsToCentsHalfUp() {
        assertThat(new Money(new BigDecimal("10.005"), "EUR").amount()).isEqualByComparingTo("10.01");
        assertThat(new Money(new BigDecimal("10.004"), "EUR").amount()).isEqualByComparingTo("10.00");
    }

    @Test
    void addsAmountsOfSameCurrency() {
        Money sum = new Money(new BigDecimal("10.50"), "EUR").add(new Money(new BigDecimal("0.75"), "EUR"));

        assertThat(sum.amount()).isEqualByComparingTo("11.25");
        assertThat(sum.currencyCode()).isEqualTo("EUR");
    }

    @Test
    void multipliesAndRounds() {
        Money result = new Money(new BigDecimal("33.33"), "EUR").multiply(new BigDecimal("3"));

        assertThat(result.amount()).isEqualByComparingTo("99.99");
    }

    @Test
    void refusesToMixCurrencies() {
        Money euro = new Money(BigDecimal.TEN, "EUR");
        Money dollar = new Money(BigDecimal.TEN, "USD");

        assertThatThrownBy(() -> euro.add(dollar)).isInstanceOf(IllegalArgumentException.class);
    }

}
