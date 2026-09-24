package com.helix.gpo.web_crm.access.internal;

import com.helix.gpo.web_crm.access.EntityType;
import com.helix.gpo.web_crm.access.PermissionAction;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
record RolePermission(
        @Enumerated(EnumType.STRING)
        @Column(name = "entity_type")
        EntityType entityType,

        @Enumerated(EnumType.STRING)
        @Column(name = "action")
        PermissionAction action
) {
}
