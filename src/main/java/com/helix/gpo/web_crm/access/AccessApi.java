package com.helix.gpo.web_crm.access;

import java.util.List;
import java.util.UUID;

public interface AccessApi {

    boolean isUnrestricted();

    boolean canRead(EntityType entity);

    boolean canWrite(EntityType entity);

    boolean canDelete(EntityType entity);

    void requireRead(EntityType entity);

    void requireWrite(EntityType entity);

    void requireDelete(EntityType entity);

    List<UUID> accessibleProjectIds();

    List<UUID> accessibleTenantIds();

    boolean canAccessProject(UUID projectId);

    boolean canAccessTenant(UUID tenantId);

}
