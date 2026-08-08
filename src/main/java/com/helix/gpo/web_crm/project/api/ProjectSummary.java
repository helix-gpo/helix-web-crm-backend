package com.helix.gpo.web_crm.project.api;

import java.util.UUID;

public record ProjectSummary(
        UUID id,
        UUID tenantId,
        String title,
        String status
) {
}
