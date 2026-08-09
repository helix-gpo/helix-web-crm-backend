package com.helix.gpo.web_crm.invoice.internal.config;

import com.helix.gpo.web_crm.shared.Address;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "helix.company")
public record CompanyBillingProperties(
        String name,
        String legalName,
        String vatId,
        Address address,
        String email,
        String iban,
        String bic
) {
}
