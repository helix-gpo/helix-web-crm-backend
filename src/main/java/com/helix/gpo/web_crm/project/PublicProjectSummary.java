package com.helix.gpo.web_crm.project;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PublicProjectSummary(
        UUID id,
        String title,
        String description,
        String fullDescription,
        List<String> highlights,
        List<ProjectTagView> tags,
        LocalDate startDate
) {

    public record ProjectTagView(String value, String colorHex) {
    }

}
