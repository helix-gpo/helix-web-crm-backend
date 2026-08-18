@org.springframework.modulith.ApplicationModule(
        displayName = "Invoice",
        allowedDependencies = {"tenant", "project", "shared", "storage", "notification"}
)
package com.helix.gpo.web_crm.invoice;