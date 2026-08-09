package com.helix.gpo.web_crm.project;

import java.util.UUID;

public record ProjectSummary(
        UUID id,
        UUID tenantId,
        String title,
        String status
) {
}
