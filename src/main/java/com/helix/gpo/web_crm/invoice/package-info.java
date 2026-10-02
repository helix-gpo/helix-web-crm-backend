@org.springframework.modulith.ApplicationModule(
        displayName = "Invoice",
        allowedDependencies = {"tenant", "project", "shared", "storage", "notification", "access"}
)
package com.helix.gpo.web_crm.invoice;