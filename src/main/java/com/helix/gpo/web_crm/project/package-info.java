// project/package-info.java
@org.springframework.modulith.ApplicationModule(
        displayName = "Project Management",
        allowedDependencies = {"tenant", "shared", "storage"}
)
package com.helix.gpo.web_crm.project;