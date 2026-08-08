package com.helix.gpo.web_crm.project.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record ProjectTag(
        @Column(name = "tag_value", length = 60)
        String value,

        @Column(name = "tag_color", length = 7)
        String colorHex
) {
}
