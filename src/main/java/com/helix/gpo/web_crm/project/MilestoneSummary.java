package com.helix.gpo.web_crm.project;

import com.helix.gpo.web_crm.shared.Money;

import java.time.LocalDate;
import java.util.UUID;

public record MilestoneSummary(
        UUID id,
        UUID projectId,
        String title,
        LocalDate dueDate,
        String status,
        Money price
) {
}
