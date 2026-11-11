package com.helix.gpo.web_crm.invoice.internal;

import com.helix.gpo.web_crm.invoice.internal.dto.InvoiceDtos;

import java.util.ArrayList;
import java.util.List;

final class BillingDataValidator {

    private BillingDataValidator() {
    }

    static List<String> missingFields(InvoiceDtos.BillingPartyDto party, String label, boolean isSeller) {
        List<String> missing = new ArrayList<>();

        if (isBlank(party.name())) {
            missing.add(label + ": Name");
        }

        // § 14 UStG: the seller states tax number OR VAT id. The buyer's VAT id
        // is only needed for EU reverse charge, so it stays optional here.
        if (isSeller && isBlank(party.vatId()) && isBlank(party.taxNumber())) {
            missing.add(label + ": Steuernummer oder USt-IdNr.");
        }

        var address = party.address();
        if (address == null
                || isBlank(address.street())
                || isBlank(address.houseNumber())
                || isBlank(address.postalCode())
                || isBlank(address.city())
                || isBlank(address.countryCode())) {
            missing.add(label + ": vollständige Anschrift");
        }

        if (isSeller && (isBlank(party.iban()) || isBlank(party.bic()))) {
            missing.add(label + ": Bankverbindung (IBAN/BIC)");
        }

        return missing;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}
