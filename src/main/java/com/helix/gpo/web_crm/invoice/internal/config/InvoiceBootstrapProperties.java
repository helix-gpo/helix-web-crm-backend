package com.helix.gpo.web_crm.invoice.internal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// env: HELIX_BOOTSTRAP_INVOICE_START_NUMBER - the first number issued in the current year
@ConfigurationProperties(prefix = "helix.bootstrap")
public record InvoiceBootstrapProperties(
        Long invoiceStartNumber
) {
}
