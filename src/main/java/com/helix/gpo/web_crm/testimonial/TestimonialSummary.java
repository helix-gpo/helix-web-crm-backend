package com.helix.gpo.web_crm.testimonial;

import java.time.Instant;
import java.util.UUID;

public record TestimonialSummary(
        UUID id,
        String partnerName,
        String partnerRole,
        String companyName,
        String description,
        int rating,
        Instant createdAt
) {
}
