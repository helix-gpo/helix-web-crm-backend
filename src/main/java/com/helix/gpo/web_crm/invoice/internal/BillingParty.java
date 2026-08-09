package com.helix.gpo.web_crm.invoice.internal;

import com.helix.gpo.web_crm.shared.Address;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public record BillingParty(
        @Column(length = 160)
        String name,

        @Column(length = 20)
        String vatId,

        @Embedded
        Address address,

        @Column(length = 254)
        String email,

        @Column(length = 34)
        String iban,

        @Column(length = 11)
        String bic
) {
}
