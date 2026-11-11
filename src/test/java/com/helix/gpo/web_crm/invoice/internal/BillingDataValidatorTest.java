package com.helix.gpo.web_crm.invoice.internal;

import com.helix.gpo.web_crm.invoice.internal.dto.InvoiceDtos.BillingPartyDto;
import com.helix.gpo.web_crm.shared.Address;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BillingDataValidatorTest {

    private static final Address ADDRESS = new Address("Erftstraße", "50", "41460", "Neuss", "DE");

    private BillingPartyDto party(String vatId, String taxNumber) {
        return new BillingPartyDto("Helix GPO", vatId, taxNumber, ADDRESS, "info@example.com", "DE00", "COBADEFFXXX");
    }

    @Test
    void sellerWithTaxNumberOnlyIsComplete() {
        assertThat(BillingDataValidator.missingFields(party(null, "122/5739/1234"), "Aussteller", true)).isEmpty();
    }

    @Test
    void sellerWithVatIdOnlyIsComplete() {
        assertThat(BillingDataValidator.missingFields(party("DE123456789", null), "Aussteller", true)).isEmpty();
    }

    @Test
    void sellerWithoutAnyTaxIdIsIncomplete() {
        assertThat(BillingDataValidator.missingFields(party(null, null), "Aussteller", true))
                .containsExactly("Aussteller: Steuernummer oder USt-IdNr.");
    }

    @Test
    void buyerWithoutVatIdIsComplete() {
        assertThat(BillingDataValidator.missingFields(party(null, null), "Mandant", false)).isEmpty();
    }
}
